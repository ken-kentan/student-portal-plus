package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.data.parser.MyClassParser
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.db.*


class MyClassDao(private val database: DatabaseOpenHelper) {

    companion object {
        const val TABLE_NAME = "my_class"
        private val PARSER = MyClassParser()
    }

    fun getAll(): List<MyClass> = database.use {
        select(TABLE_NAME)
                .orderBy("week")
                .orderBy("period")
                .orderBy("user", SqlOrderDirection.DESC)
                .parseList(PARSER)
    }

    fun getSubjectList(): List<String> = database.use {
        select(TABLE_NAME)
                .distinct()
                .whereArgs("subject")
                .parseList(object : RowParser<String> {
                    override fun parseRow(columns: Array<Any?>) = columns[0] as String
                })
    }

    fun get(id: Long): MyClass? = database.use {
        select(TABLE_NAME)
                .whereArgs("_id=$id")
                .limit(1)
                .parseOpt(PARSER)
    }

    fun updateAll(list: List<MyClass>) = database.use {
        transaction {
            var st = compileStatement("INSERT OR IGNORE INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?,?,?);")

            // Insert new data
            list.forEach {
                st.bindNull(1)
                st.bindLong(2, it.hash)
                st.bindLong(3, it.week.code.toLong())
                st.bindLong(4, it.period.toLong())
                st.bindString(5, it.scheduleCode)
                st.bindLong(6, it.credit.toLong())
                st.bindString(7, it.category)
                st.bindString(8, it.subject)
                st.bindString(9, it.instructor)
                st.bindLong(10, it.isUser.toLong())
                st.bindLong(11, it.color.toLong())
                st.bindStringOrNull(12, it.location)

                st.executeInsert()
                st.clearBindings()
            }

            // Delete old data
            if (list.isNotEmpty()) {
                val args = StringBuilder("?")
                for (i in 2..list.size) {
                    args.append(",?")
                }

                st = compileStatement("DELETE FROM $TABLE_NAME WHERE user=0 AND hash NOT IN ($args)")
                list.forEachIndexed { i, d ->
                    st.bindLong(i+1, d.hash)
                }

                st.executeUpdateDelete()
            } else {
                delete(TABLE_NAME, "user=0")
            }
        }
    }

    fun update(data: MyClass) = database.use {
        update(TABLE_NAME,
                "hash"           to data.hash,
                "week"           to data.week.code,
                "period"         to data.period,
                "schedule_code"  to data.scheduleCode,
                "credit"         to data.credit,
                "category"       to data.category,
                "subject"        to data.subject,
                "instructor"     to data.instructor,
                "user"           to data.isUser,
                "color"          to data.color,
                "location"       to data.location)
                .whereArgs("_id=${data.id}")
                .exec()
    }

    fun add(list: List<MyClass>) = database.use {
        var count = 0

        transaction {
            val st = compileStatement("INSERT INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?,?,?);")

            list.forEach{
                st.bindNull(1)
                st.bindLong(2, it.hash)
                st.bindLong(3, it.week.code.toLong())
                st.bindLong(4, it.period.toLong())
                st.bindString(5, it.scheduleCode)
                st.bindLong(6, it.credit.toLong())
                st.bindString(7, it.category)
                st.bindString(8, it.subject)
                st.bindString(9, it.instructor)
                st.bindLong(10, it.isUser.toLong())
                st.bindLong(11, it.color.toLong())
                st.bindStringOrNull(12, it.location)

                st.executeInsert()
                st.clearBindings()

                count++
            }
        }

        return@use count
    }

    fun delete(subject: String) = database.use {
        delete(TABLE_NAME, "subject='${subject.escapeQuery()}' AND user=1")
    }
}