package jp.kentan.studentportalplus.data.dao

import android.content.Context
import jp.kentan.studentportalplus.data.component.NoticeData
import jp.kentan.studentportalplus.data.parser.NoticeParser
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.collections.forEachReversedByIndex
import org.jetbrains.anko.db.select


class NoticeDao(val context: Context) {

    companion object {
        const val TABLE_NAME = "notice"
        private val PARSER = NoticeParser()
    }

    fun getAll(): List<NoticeData> = context.database.use {
        select(TABLE_NAME).parseList(PARSER)
    }

    fun updateAll(list: List<NoticeData>) = context.database.use {
        beginTransaction()

        var st = compileStatement("INSERT OR IGNORE INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?);")

        // Insert new data
        list.forEachReversedByIndex {
            st.bindNull(1)
            st.bindLong(2, it.hash.toLong())
            st.bindString(3, DatabaseOpenHelper.toString(it.createdDate))
            st.bindString(4, it.inCharge)
            st.bindString(5, it.category)
            st.bindString(6, it.title)
            st.bindStringOrNull(7, it.detail)
            st.bindStringOrNull(8, it.link)
            st.bindLong(9, it.hasRead.toLong())
            st.bindLong(10, it.isFavorite.toLong())

            st.executeInsert()
            st.clearBindings()
        }

        // Delete old data
        if (list.isNotEmpty()) {
            val args = StringBuilder("?")
            for (i in 1..list.size) {
                args.append(",?")
            }

            st = compileStatement("DELETE FROM $TABLE_NAME WHERE favorite=0 AND hash NOT IN ($args)")
            list.forEachIndexed { i, d ->
                st.bindLong(i+1, d.hash.toLong())
            }

            st.executeUpdateDelete()
        } else {
            delete(TABLE_NAME, "favorite", arrayOf("0"))
        }

        setTransactionSuccessful()
        endTransaction()
    }
}