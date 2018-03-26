package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.parser.LectureAttendParser
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update


class LectureInformationDao(private val database: DatabaseOpenHelper) {

    companion object {
        const val TABLE_NAME = "lecture_info"

        private val PARSER = LectureInformationParser()
        private val LECTURE_ATTEND_PARSER = LectureAttendParser()

        private val STRING_DISTANCE = JaroWinklerDistance()
    }

    fun getAll(): List<LectureInformation> = database.use {
        val myClassList = select(MyClassDao.TABLE_NAME, "subject, attend").parseList(LECTURE_ATTEND_PARSER)

        val lectureInfoList = select(TABLE_NAME)
                .orderBy("DATE(updated_date)", SqlOrderDirection.DESC)
                .orderBy("subject")
                .parseList(PARSER)

        return@use lectureInfoList.map {
            val subject = it.subject
            var attend  = LectureAttendType.NOT

            myClassList.forEach {
                if (it.first == subject) {
                    attend = it.second
                    return@forEach
                } else if (STRING_DISTANCE.getDistance(it.first, subject) >= 0.8f) {
                    attend = LectureAttendType.SIMILAR
                }
            }

            it.copy(attend = attend)
        }
    }

    fun updateAll(list: List<LectureInformation>) = database.use {
        beginTransaction()

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
            st.bindString(12, DatabaseOpenHelper.toString(it.createdDate))
            st.bindString(13, DatabaseOpenHelper.toString(it.updatedDate))
            st.bindLong(14, it.hasRead.toLong())

            st.executeInsert()
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
                st.bindLong(i+1, d.hash)
            }

            st.executeUpdateDelete()
        } else {
            delete(TABLE_NAME)
        }

        setTransactionSuccessful()
        endTransaction()
    }

    fun update(data: LectureInformation): Int = database.use {
        update(TABLE_NAME, "read" to data.hasRead.toLong())
                .whereArgs("_id = ${data.id}")
                .exec()
    }
}