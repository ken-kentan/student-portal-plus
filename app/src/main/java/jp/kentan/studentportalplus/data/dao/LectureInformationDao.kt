package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.component.LectureOrderType
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.component.NotifyContent
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.parser.LectureAttendParser
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update


class LectureInformationDao(
        private val database: DatabaseOpenHelper,
        var myClassThreshold: Float
) {

    companion object {
        const val TABLE_NAME = "lecture_info"

        private val PARSER = LectureInformationParser()
        private val LECTURE_ATTEND_PARSER = LectureAttendParser()

        private val STRING_DISTANCE = JaroWinklerDistance()
    }

    fun getAll(): List<LectureInformation> = database.use {
        val myClassList = select(MyClassDao.TABLE_NAME, "subject, user").parseList(LECTURE_ATTEND_PARSER)

        select(TABLE_NAME)
                .orderBy("DATE(updated_date)", SqlOrderDirection.DESC)
                .orderBy("subject")
                .parseList(PARSER)
                .map {
                    it.copy(attend = myClassList.analyzeAttendType(it.subject))
                }
    }

    fun get(id: Long): LectureInformation? = database.use {
        val myClassList = select(MyClassDao.TABLE_NAME, "subject, user").parseList(LECTURE_ATTEND_PARSER)

        val data = select(TABLE_NAME)
                .whereArgs("_id=$id")
                .limit(1)
                .parseOpt(PARSER) ?: return@use null

        data.copy(attend = myClassList.analyzeAttendType(data.subject))
    }

    fun search(query: LectureQuery) = database.use {
        val myClassList = select(MyClassDao.TABLE_NAME, "subject, user").parseList(LECTURE_ATTEND_PARSER)

        val where = StringBuilder()

        if (!query.isAttend) {
            if (!query.isUnread && !query.hasRead) {
                return@use emptyList<LectureInformation>()
            } else if (!query.isUnread) {
                where.append("read=1")
            } else if (!query.hasRead) {
                where.append("read=0")
            }
        }

        if (query.keywordList.isNotEmpty()) {
            where.appendIfNotEmpty(" AND ")
            where.append('(')

            // Subject
            query.keywordList.forEach { where.append("subject LIKE '%$it%' AND ") }
            where.delete(where.length-5, where.length)

            where.append(") OR (")

            // Instructor
            query.keywordList.forEach { where.append("instructor LIKE '%$it%' AND ") }
            where.delete(where.length-5, where.length)

            where.append(") ")
        }

        val lectureInfoList = select(TABLE_NAME)
                .whereArgs(where.toString())
                .orderBy("DATE(updated_date)", SqlOrderDirection.DESC)
                .orderBy("subject")
                .parseList(PARSER)

        val result = lectureInfoList.mapNotNull {
            val type  = myClassList.analyzeAttendType(it.subject)

            if (query.isAttend && !type.isAttend()) {
                if (!query.isUnread && !query.hasRead) {
                    return@mapNotNull null
                } else if (!query.hasRead && it.isRead) {
                    return@mapNotNull null
                }

                it.copy(attend = type)
            } else if (!query.isAttend && type.isAttend()) {
                null
            } else {
                it.copy(attend = type)
            }
        }

        return@use if (query.order == LectureOrderType.ATTEND_CLASS) {
            result.sortedBy { !it.attend.isAttend() }
        } else {
            result
        }
    }

    fun updateAll(list: List<LectureInformation>) = database.use {
        beginTransaction()

        val notifyDataList = mutableListOf<NotifyContent>()

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
            st.bindLong(14, it.isRead.toLong())

            val id = st.executeInsert()
            if (id > 0) {
                notifyDataList.add(NotifyContent(it.subject, it.detailText, id))
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
                st.bindLong(i+1, d.hash)
            }

            st.executeUpdateDelete()
        } else {
            delete(TABLE_NAME)
        }

        setTransactionSuccessful()
        endTransaction()

        return@use notifyDataList
    }

    fun update(data: LectureInformation): Int = database.use {
        update(TABLE_NAME, "read" to data.isRead.toLong())
                .whereArgs("_id = ${data.id}")
                .exec()
    }

    private fun List<Pair<String, LectureAttendType>>.analyzeAttendType(subject: String): LectureAttendType {
        var type  = LectureAttendType.NOT

        forEach {
            if (it.first == subject) {
                return it.second
            } else if (STRING_DISTANCE.getDistance(it.first, subject) >= myClassThreshold) {
                type = LectureAttendType.SIMILAR
            }
        }

        return type
    }
}