package jp.kentan.studentportalplus.data.dao

import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getStringOrNull
import jp.kentan.studentportalplus.data.component.ClassWeek
import jp.kentan.studentportalplus.util.Murmur3
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import kotlin.math.max

class DatabaseMigrationHelper {
    companion object {

        fun upgradeVersion3From2(db: SQLiteDatabase, createTablesIfNotExist: (SQLiteDatabase) -> Unit) {
            db.transaction {
                db.execSQL("ALTER TABLE my_class RENAME TO tmp_my_class")

                db.dropTable("news", true)
                db.dropTable("lecture_info", true)
                db.dropTable("lecture_cancel", true)
                db.dropTable("my_class", true)

                db.execSQL("DELETE FROM sqlite_sequence")

                createTablesIfNotExist(db)

                db.select("tmp_my_class").exec {
                    while (moveToNext()) {
                        val week: ClassWeek = getLong(1).let {
                            return@let ClassWeek.valueOf(it.toInt() + 1)
                        }
                        val period = getLong(2).let { if (it > 0) it else 0 }
                        val scheduleCode = getLong(8).let { code ->
                            if (code < 0L) "" else code.toString()
                        }
                        val credit = max(getLong(7), 0)
                        val category = getStringOrNull(6) ?: ""
                        val subject = getString(3)
                        val isUser = getLong(10) == 1L
                        val instructor = getStringOrNull(4)?.let {
                            return@let if (isUser) it else it.replace(' ', '　')
                        } ?: ""
                        val location = getStringOrNull(5)?.let {
                            val trim = it.trim()
                            return@let if (trim.isBlank()) null else trim
                        }

                        val hash = Murmur3.hash64("$week$period$scheduleCode$credit$category$subject$instructor$isUser")

                        db.insert("my_class",
                                "_id" to null,
                                "hash" to hash,
                                "week" to week.code,
                                "period" to period,
                                "schedule_code" to scheduleCode,
                                "credit" to credit,
                                "category" to category,
                                "subject" to subject,
                                "instructor" to instructor,
                                "user" to isUser.toLong(),
                                "color" to getLong(9),
                                "location" to location)
                    }
                }

                db.dropTable("tmp_my_class")
            }
        }

        private fun Boolean.toLong() = if (this) 1L else 0L
    }
}