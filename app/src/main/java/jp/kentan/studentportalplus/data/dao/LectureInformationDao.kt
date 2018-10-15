package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.LectureAttend
import jp.kentan.studentportalplus.data.component.PortalContent
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.parser.LectureAttendParser
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update

class LectureInformationDao(
        private val database: DatabaseOpenHelper,
        var similarThreshold: Float
) : BaseDao() {

    companion object {
        const val TABLE_NAME = "lecture_info"

        private val PARSER = LectureInformationParser()
        private val LECTURE_ATTEND_PARSER = LectureAttendParser()

        private val STRING_DISTANCE = JaroWinklerDistance()
    }

    fun getAll(): List<LectureInformation> = database.use {
        val myClassList = select(MyClassDao.TABLE_NAME, "subject, user").parseList(LECTURE_ATTEND_PARSER)

        select(TABLE_NAME)
                .orderBy("updated_date", SqlOrderDirection.DESC)
                .orderBy("subject")
                .parseList(PARSER)
                .map { it.copy(attend = myClassList.calcLectureAttend(it.subject)) }
    }

    fun update(data: LectureInformation): Int = database.use {
        update(TABLE_NAME, "read" to data.isRead.toLong())
                .whereArgs("_id = ${data.id}")
                .exec()
    }

    fun updateAll(list: List<LectureInformation>) = database.use {
        beginTransaction()

        val updatedContentList = mutableListOf<PortalContent>()

        var st = compileStatement("INSERT OR IGNORE INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?);")

        // Insert new data
        list.forEach {
            st.bindNull(1)
            st.bindLong(2, it.hash)
            st.bindString(3, it.grade)
            st.bindString(4, it.semester)
            st.bindString(5, it.subject)
            st.bindString(6, it.instructor)
            st.bindString(7, it.week)
            st.bindString(8, it.period)
            st.bindString(9, it.category)
            st.bindString(10, it.detailText)
            st.bindString(11, it.detailHtml)
            st.bindLong(12, it.createdDate.time)
            st.bindLong(13, it.updatedDate.time)
            st.bindLong(14, it.isRead.toLong())

            val id = st.executeInsert()
            if (id > 0) {
                updatedContentList.add(PortalContent(id, it.subject, it.detailText))
            }
            st.clearBindings()
        }

        // Delete old data
        if (list.isNotEmpty()) {
            val args = StringBuilder("?")
            for (i in 2..list.size) {
                args.append(",?")
            }

            st = compileStatement("DELETE FROM $TABLE_NAME WHERE hash NOT IN ($args)")
            list.forEachIndexed { i, d ->
                st.bindLong(i + 1, d.hash)
            }

            st.executeUpdateDelete()
        } else {
            delete(TABLE_NAME)
        }

        setTransactionSuccessful()
        endTransaction()

        return@use updatedContentList
    }

    private fun List<Pair<String, LectureAttend>>.calcLectureAttend(subject: String): LectureAttend {
        // If match subject
        firstOrNull { it.first == subject }?.run {
            return second
        }

        // If similar
        if (any { STRING_DISTANCE.getDistance(it.first, subject) >= similarThreshold }) {
            return LectureAttend.SIMILAR
        }

        return LectureAttend.NOT
    }
}