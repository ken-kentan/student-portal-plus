package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.CreatedDateType
import jp.kentan.studentportalplus.data.component.NoticeQuery
import jp.kentan.studentportalplus.data.component.NotifyContent
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.data.parser.NoticeParser
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.collections.forEachReversedByIndex
import org.jetbrains.anko.db.SqlOrderDirection
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update
import java.util.*


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

    fun search(query: NoticeQuery) = database.use {
        val where = StringBuilder()

        if (query.isFavorite) {
            if (!query.isUnread || !query.hasRead) {
                when {
                    query.isUnread -> where.append("(read=0 OR favorite=1)")
                    query.hasRead  -> where.append("(read=1 OR favorite=1)")
                    else -> where.append("favorite=1")
                }
            }
        } else {
            if (query.isUnread && query.hasRead) {
                where.append("favorite=0")
            } else if (query.isUnread) {
                where.append("(read=0 AND favorite=0)")
            } else if (query.hasRead) {
                where.append("(read=1 AND favorite=0)")
            } else {
                where.append("(read=0 AND read=1 AND favorite=0)")
            }
        }

        if (query.type != CreatedDateType.ALL) {
            where.appendIfNotEmpty(" AND ")

            val calendar = Calendar.getInstance()
            when(query.type) {
                CreatedDateType.WEEK  -> { calendar.set(Calendar.DAY_OF_WEEK , calendar.firstDayOfWeek) }
                CreatedDateType.MONTH -> { calendar.set(Calendar.DAY_OF_MONTH, 1) }
                CreatedDateType.YEAR  -> { calendar.set(Calendar.DAY_OF_YEAR , 1) }
                else -> {}
            }

            where.append("created_date>=DATE('${DatabaseOpenHelper.toString(calendar.time)}')")
        }

        if (query.keywordList.isNotEmpty()) {
            where.appendIfNotEmpty(" AND ")

            query.keywordList.forEach {
                        where.append("title LIKE '%${it.escapeQuery()}%' AND ")
                    }

            where.delete(where.length-5, where.length)
        }

        select(TABLE_NAME)
                .whereArgs(where.toString())
                .orderBy("created_date", SqlOrderDirection.DESC)
                .orderBy("_id", SqlOrderDirection.DESC)
                .parseList(PARSER)
    }

    fun updateAll(list: List<Notice>) = database.use {
        beginTransaction()

        val notifyDataList = mutableListOf<NotifyContent>()

        var st = compileStatement("INSERT OR IGNORE INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?,?);")

        // Insert new data
        list.forEachReversedByIndex {
            st.bindNull(1)
            st.bindLong(2, it.hash)
            st.bindString(3, DatabaseOpenHelper.toString(it.createdDate))
            st.bindString(4, it.inCharge)
            st.bindString(5, it.category)
            st.bindString(6, it.title)
            st.bindStringOrNull(7, it.detailText)
            st.bindStringOrNull(8, it.detailHtml)
            st.bindStringOrNull(9, it.link)
            st.bindLong(10, it.hasRead.toLong())
            st.bindLong(11, it.isFavorite.toLong())

            val id = st.executeInsert()
            if (id > 0) {
                notifyDataList.add(NotifyContent(it.title, it.detailText ?: it.link, id))
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
                st.bindLong(i+1, d.hash)
            }

            st.executeUpdateDelete()
        } else {
            delete(TABLE_NAME, "favorite=0")
        }

        setTransactionSuccessful()
        endTransaction()

        return@use notifyDataList
    }

    fun update(data: Notice): Int = database.use {
        update(TABLE_NAME, "favorite" to data.isFavorite.toLong(), "read" to data.hasRead.toLong())
                .whereArgs("_id = ${data.id}")
                .exec()
    }
}