package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.data.parser.MyClassParser
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction


class MyClassDao(private val database: DatabaseOpenHelper) {

    companion object {
        const val TABLE_NAME = "my_class"
        private val PARSER = MyClassParser()
    }

    fun getAll(): List<MyClass> = database.use {
        select(TABLE_NAME).orderBy("week").orderBy("period").parseList(PARSER)
    }

    fun updateAll(list: List<MyClass>) = database.use {
        transaction {
            var st = compileStatement("INSERT OR IGNORE INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?,?);")

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
                st.bindStringOrNull(11, it.location)

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
                delete(TABLE_NAME, "attend='${LectureAttendType.PORTAL.name}'")
            }
        }
    }

    fun add(list: List<MyClass>) = database.use {
        transaction {
            val st = compileStatement("INSERT INTO $TABLE_NAME VALUES(?,?,?,?,?,?,?,?,?,?,?);")

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
                st.bindStringOrNull(11, it.location)

                st.executeInsert()
                st.clearBindings()
            }
        }
    }

    fun delete(subject: String) = database.use {
        delete(TABLE_NAME, "subject='${subject.escapeQuery()}'")
    }
}