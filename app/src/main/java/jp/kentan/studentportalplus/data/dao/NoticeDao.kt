package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.PortalContent
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.data.parser.NoticeParser
import org.jetbrains.anko.collections.forEachReversedByIndex
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update

class NoticeDao(
        private val database: DatabaseOpenHelper
) : BaseDao() {

    companion object {
        const val TABLE_NAME = "notice"
        private val PARSER = NoticeParser()
    }

    fun getAll(): List<Notice> = database.use {
        select(TABLE_NAME)
                .orderBy("created_date", SqlOrderDirection.DESC)
                .orderBy("_id", SqlOrderDirection.DESC)
                .parseList(PARSER)
    }

    fun update(data: Notice): Int = database.use {
        update(TABLE_NAME, "favorite" to data.isFavorite.toLong(), "read" to data.isRead.toLong())
                .whereArgs("_id = ${data.id}")
                .exec()
    }

    fun updateAll(list: List<Notice>) = database.use {
        beginTransaction()

        val updatedContentList = mutableListOf<PortalContent>()

        var st = compileStatement("INSERT OR IGNORE INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?,?);")

        // Insert new data
        list.forEachReversedByIndex {
            st.bindNull(1)
            st.bindLong(2, it.hash)
            st.bindLong(3, it.createdDate.time)
            st.bindString(4, it.inCharge)
            st.bindString(5, it.category)
            st.bindString(6, it.title)
            st.bindStringOrNull(7, it.detailText)
            st.bindStringOrNull(8, it.detailHtml)
            st.bindStringOrNull(9, it.link)
            st.bindLong(10, it.isRead.toLong())
            st.bindLong(11, it.isFavorite.toLong())

            val id = st.executeInsert()
            if (id > 0) {
                updatedContentList.add(PortalContent(id, it.title, it.detailText ?: it.link))
            }
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
                st.bindLong(i + 1, d.hash)
            }

            st.executeUpdateDelete()
        } else {
            delete(TABLE_NAME, "favorite=0")
        }

        setTransactionSuccessful()
        endTransaction()

        return@use updatedContentList
    }
}