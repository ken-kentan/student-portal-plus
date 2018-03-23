package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.Notice
import jp.kentan.studentportalplus.data.parser.NoticeParser
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.collections.forEachReversedByIndex
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update


class NoticeDao(private val database: DatabaseOpenHelper) {

    companion object {
        const val TABLE_NAME = "notice"
        private val PARSER = NoticeParser()
    }

    fun getAll(): List<Notice> = database.use {
        select(TABLE_NAME).orderBy("created_date", SqlOrderDirection.DESC).orderBy("_id", SqlOrderDirection.DESC).parseList(PARSER)
    }

    fun get(id: Long): Notice? = database.use {
        select(TABLE_NAME).whereArgs("_id=$id").limit(1).parseOpt(PARSER)
    }

    fun updateAll(list: List<Notice>) = database.use {
        beginTransaction()

        var st = compileStatement("INSERT OR IGNORE INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?);")

        // Insert new data
        list.forEachReversedByIndex {
            st.bindNull(1)
            st.bindLong(2, it.hash)
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
            for (i in 2..list.size) {
                args.append(",?")
            }

            st = compileStatement("DELETE FROM $TABLE_NAME WHERE favorite=0 AND hash NOT IN ($args)")
            list.forEachIndexed { i, d ->
                st.bindLong(i+1, d.hash)
            }

            st.executeUpdateDelete()
        } else {
            delete(TABLE_NAME, "favorite=0")
        }

        setTransactionSuccessful()
        endTransaction()
    }

    fun update(data: Notice): Int = database.use {
        update(TABLE_NAME, "favorite" to data.isFavorite.toLong(), "read" to data.hasRead.toLong())
                .whereArgs("_id = ${data.id}")
                .exec()
    }
}