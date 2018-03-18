package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.component.LectureCancellation
import jp.kentan.studentportalplus.data.parser.LectureAttendParser
import jp.kentan.studentportalplus.data.parser.LectureCancellationParser
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update


class LectureCancellationDao(private val database: DatabaseOpenHelper) {

    companion object {
        const val TABLE_NAME = "lecture_cancel"

        private val PARSER = LectureCancellationParser()
        private val LECTURE_ATTEND_PARSER = LectureAttendParser()

        private val STRING_DISTANCE = JaroWinklerDistance()
    }

    fun getAll(): List<LectureCancellation> = database.use {
        val myClassList = select(MyClassDao.TABLE_NAME, "subject, attend").parseList(LECTURE_ATTEND_PARSER)

        val lectureCancelList = select(TABLE_NAME)
                .orderBy("DATE(created_date)", SqlOrderDirection.DESC)
                .orderBy("subject")
                .parseList(PARSER)

        return@use lectureCancelList.map {
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

    fun updateAll(list: List<LectureCancellation>) = database.use {
        beginTransaction()

        var st = compileStatement("INSERT OR IGNORE INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?,?);")

        // Insert new data
        list.forEach {
            st.bindNull(1)
            st.bindLong(2, it.hash.toLong())
            st.bindString(3, it.grade)
            st.bindString(4, it.subject)
            st.bindString(5, it.instructor)
            st.bindString(6, DatabaseOpenHelper.toString(it.cancelDate))
            st.bindString(7, it.week)
            st.bindString(8, it.period)
            st.bindString(9, it.detail)
            st.bindString(10, DatabaseOpenHelper.toString(it.createdDate))
            st.bindLong(11, it.hasRead.toLong())

            st.executeInsert()
            st.clearBindings()
        }

        // Delete old data
        if (list.isNotEmpty()) {
            val args = StringBuilder("?")
            for (i in 1..list.size) {
                args.append(",?")
            }

            st = compileStatement("DELETE FROM $TABLE_NAME WHERE hash NOT IN ($args)")
            list.forEachIndexed { i, d ->
                st.bindLong(i+1, d.hash.toLong())
            }

            st.executeUpdateDelete()
        } else {
            delete(TABLE_NAME)
        }

        setTransactionSuccessful()
        endTransaction()
    }

    fun update(data: LectureCancellation): Int = database.use {
        update(TABLE_NAME, "read" to data.hasRead.toLong())
                .whereArgs("_id = ${data.id}")
                .exec()
    }
}