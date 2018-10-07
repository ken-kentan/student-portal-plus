package jp.kentan.studentportalplus.data.dao

import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getStringOrNull
import jp.kentan.studentportalplus.data.component.ClassWeek
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction

class DatabaseMigrationHelper {
    companion object {

        fun upgradeVersion3From2(db: SQLiteDatabase, createTablesIfNotExist:(SQLiteDatabase) -> Unit) {
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
                        val week: ClassWeek = getLong(2).let {
                            return@let ClassWeek.valueOf(it.toInt() + 1)
                        }
                        val period       = getLong(3).let { if (it > 0) it else 0 }
                        val scheduleCode = getLong(9).toString()
                        val credit       = getLong(8)
                        val category     = getStringOrNull(7) ?: ""
                        val subject      = getString(4)
                        val isUser       = getLong(11) == 1L
                        val instructor   = getStringOrNull(5).let { it ?: return@let ""
                            return@let if (isUser) { it } else { it.replace(' ', '　') }
                        }
                        val location = getStringOrNull(6)?.let {
                            val trim = it.trim()
                            return@let if (trim.isBlank()) null else trim
                        }

                        db.insert("my_class",
                                "_id" to null,
                                "week"           to week.code,
                                "period"         to period,
                                "schedule_code"  to scheduleCode,
                                "credit"         to credit,
                                "category"       to category,
                                "subject"        to subject,
                                "instructor"     to instructor,
                                "user"           to isUser.toLong(),
                                "color"          to getLong(10),
                                "location"       to location)
                    }
                }

                db.dropTable("tmp_my_class")
            }
        }

        private fun Boolean.toLong() = if (this) 1L else 0L
    }
}