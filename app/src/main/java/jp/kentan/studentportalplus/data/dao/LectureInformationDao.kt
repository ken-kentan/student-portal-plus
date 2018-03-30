package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.component.LectureOrderType
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

        private val EMPTY = listOf<LectureInformation>()
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

    fun search(keywords: String?, orderType: LectureOrderType, isUnread: Boolean, hasRead: Boolean, isAttend: Boolean) = database.use {
        val myClassList = select(MyClassDao.TABLE_NAME, "subject, user").parseList(LECTURE_ATTEND_PARSER)

        val where = StringBuilder()

        if (!isAttend) {
            if (!isUnread && !hasRead) {
                return@use EMPTY
            }

            if (!isUnread) {
                where.append("read=1")
            } else if (!hasRead) {
                where.append("read=0")
            }
        }

        if (keywords != null) {
            where.appendIfNotEmpty(" AND ")
            where.append('(')

            val keywordList = keywords.split(' ')
                    .mapNotNull {
                        val trim = it.trim()
                        if (trim.isNotEmpty()) trim.escapeQuery() else null
                    }

            // Subject
            keywordList.forEach { where.append("subject LIKE '%$it%' AND ") }
            where.delete(where.length-5, where.length)

            where.append(") OR (")

            // Instructor
            keywordList.forEach { where.append("instructor LIKE '%$it%' AND ") }
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
            it.copy(attend = type)
//            if (isAttend) {
//                null
//            } else if (!isAttend && type.isAttend()) {
//                null
//            } else {
//                it.copy(attend = type)
//            }
        }

        return@use if (orderType == LectureOrderType.ATTEND_CLASS) {
            result.sortedBy { it.attend.isAttend() }
        } else {
            result
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

    private fun List<Pair<String, LectureAttendType>>.analyzeAttendType(subject: String): LectureAttendType {
        var type  = LectureAttendType.NOT

        for (i in this) {
            if (i.first == subject) {
                type = i.second
                break
            } else if (STRING_DISTANCE.getDistance(i.first, subject) >= 0.8f) {
                type = LectureAttendType.SIMILAR
            }
        }

        return type
    }
}