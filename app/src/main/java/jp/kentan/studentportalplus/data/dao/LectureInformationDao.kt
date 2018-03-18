package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.LectureInformation
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update


class LectureInformationDao(private val database: DatabaseOpenHelper) {

    companion object {
        const val TABLE_NAME = "lecture_info"
        private val PARSER = LectureInformationParser()
    }

    fun getAll(): List<LectureInformation> = database.use {
        select(TABLE_NAME).orderBy("updated_date", SqlOrderDirection.DESC).parseList(PARSER)
    }

    fun updateAll(list: List<LectureInformation>) = database.use {
        beginTransaction()

        var st = compileStatement("INSERT OR IGNORE INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?);")

        // Insert new data
        list.forEach {
            st.bindNull(1)
            st.bindLong(2, it.hash.toLong())
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

    fun update(data: LectureInformation): Int = database.use {
        update(TABLE_NAME, "read" to data.hasRead.toLong())
                .whereArgs("_id = ${data.id}")
                .exec()
    }
}