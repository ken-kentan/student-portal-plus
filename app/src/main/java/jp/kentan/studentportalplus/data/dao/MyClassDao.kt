package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.component.MyClass
import jp.kentan.studentportalplus.data.parser.MyClassParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select


class MyClassDao(private val database: DatabaseOpenHelper) {

    companion object {
        const val TABLE_NAME = "my_class"
        private val PARSER = MyClassParser()
    }

    fun getAll(): List<MyClass> = database.use {
        select(TABLE_NAME).orderBy("week").orderBy("period").parseList(PARSER)
    }

    fun updateAll(list: List<MyClass>) = database.use {
        beginTransaction()

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
            st.bindString(10, it.attend.name)
            st.bindStringOrNull(11, it.location)

            st.executeInsert()
            st.clearBindings()
        }

        // Delete old data
        if (list.isNotEmpty()) {
            val args = StringBuilder("?")
            for (i in 1..list.size) {
                args.append(",?")
            }

            st = compileStatement("DELETE FROM $TABLE_NAME WHERE attend='${LectureAttendType.PORTAL.name}' AND hash NOT IN ($args)")
            list.forEachIndexed { i, d ->
                st.bindLong(i+1, d.hash)
            }

            st.executeUpdateDelete()
        } else {
            delete(TABLE_NAME, "attend='${LectureAttendType.PORTAL.name}'")
        }

        setTransactionSuccessful()
        endTransaction()
    }

//    fun update(data: MyClass): Int = database.use {
//        update(TABLE_NAME, "read" to data.hasRead.toLong())
//                .whereArgs("_id = ${data.id}")
//                .exec()
//    }
}