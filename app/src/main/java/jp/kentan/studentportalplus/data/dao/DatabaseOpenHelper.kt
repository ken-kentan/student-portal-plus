package jp.kentan.studentportalplus.data.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import androidx.core.database.getLong
import androidx.core.database.getString
import androidx.core.database.getStringOrNull
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.util.Murmur3
import jp.kentan.studentportalplus.util.toLong
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * DatabaseOpenHelper
 *
 * https://github.com/Kotlin/anko/wiki/Anko-SQLite
 */
class DatabaseOpenHelper(context: Context) : ManagedSQLiteOpenHelper(context, "portal_data.db", null, version = 3) {

    companion object {
        private var instance: DatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): DatabaseOpenHelper {
            if (instance == null) {
                instance = DatabaseOpenHelper(ctx.applicationContext)
            }
            return instance!!
        }


        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)

        fun toString(date: Date): String = DATE_FORMAT.format(date)
        fun toDate(date: String): Date   = DATE_FORMAT.parse(date)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTablesIfNotExist(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 2) {
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
                        val week: ClassWeekType = getLong("day_of_week").let {
                            return@let ClassWeekType.valueOf(it.toInt() + 1)
                        }
                        val period       = getLong("period").let { if (it > 0) it else 0 }
                        val scheduleCode = getLong("timetable_number").toString()
                        val credit       = getLong("credits")
                        val category     = getStringOrNull("type") ?: ""
                        val subject      = getString("subject")
                        val isUser       = getLong("registered_by_user") == 1L
                        val instructor   = getStringOrNull("instructor").let { it ?: return@let ""
                            return@let if (isUser) { it } else { it.replace(' ', '　') }
                        }

                        val hashStr = week.name + period + scheduleCode + credit + category + subject + instructor + isUser

                        db.insert("my_class",
                                "_id" to null,
                                "hash"           to Murmur3.hash64(hashStr.toByteArray()),
                                "week"           to week.code,
                                "period"         to period,
                                "schedule_code"  to scheduleCode,
                                "credit"         to credit,
                                "category"       to category,
                                "subject"        to subject,
                                "instructor"     to instructor,
                                "user"           to isUser.toLong(),
                                "color"          to getLong("color_rgb"),
                                "location"       to getStringOrNull("place"))
                    }
                }

                db.dropTable("tmp_my_class")
            }
        }
    }

    private fun createTablesIfNotExist(db: SQLiteDatabase) {
        db.createTable(NoticeDao.TABLE_NAME, true,
                "_id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "hash"         to INTEGER + NOT_NULL + UNIQUE,
                "created_date" to TEXT + NOT_NULL,
                "in_charge"    to TEXT + NOT_NULL,
                "category"     to TEXT + NOT_NULL,
                "title"        to TEXT + NOT_NULL,
                "detail_text"  to TEXT,
                "detail_html"  to TEXT,
                "link"         to TEXT,
                "read"         to INTEGER + NOT_NULL + DEFAULT("0"),
                "favorite"     to INTEGER + NOT_NULL + DEFAULT("0"))

        db.createTable(LectureInformationDao.TABLE_NAME, true,
                "_id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "hash"         to INTEGER + NOT_NULL + UNIQUE,
                "grade"        to TEXT + NOT_NULL,
                "semester"     to TEXT + NOT_NULL,
                "subject"      to TEXT + NOT_NULL,
                "instructor"   to TEXT + NOT_NULL,
                "week"         to TEXT + NOT_NULL,
                "period"       to TEXT + NOT_NULL,
                "category"     to TEXT + NOT_NULL,
                "detail_text"  to TEXT + NOT_NULL,
                "detail_html"  to TEXT + NOT_NULL,
                "created_date" to TEXT + NOT_NULL,
                "updated_date" to TEXT + NOT_NULL,
                "read"         to INTEGER + NOT_NULL + DEFAULT("0"))

        db.createTable(LectureCancellationDao.TABLE_NAME, true,
                "_id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "hash"         to INTEGER + NOT_NULL + UNIQUE,
                "grade"        to TEXT + NOT_NULL,
                "subject"      to TEXT + NOT_NULL,
                "instructor"   to TEXT + NOT_NULL,
                "cancel_date"  to TEXT + NOT_NULL,
                "week"         to TEXT + NOT_NULL,
                "period"       to TEXT + NOT_NULL,
                "detail_text"  to TEXT + NOT_NULL,
                "detail_html"  to TEXT + NOT_NULL,
                "created_date" to TEXT + NOT_NULL,
                "read"         to INTEGER + NOT_NULL + DEFAULT("0"))

        db.createTable(MyClassDao.TABLE_NAME, true,
                "_id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "hash"           to INTEGER + NOT_NULL + UNIQUE,
                "week"           to INTEGER + NOT_NULL,
                "period"         to INTEGER + NOT_NULL,
                "schedule_code"  to TEXT    + NOT_NULL,
                "credit"         to INTEGER + NOT_NULL,
                "category"       to TEXT    + NOT_NULL,
                "subject"        to TEXT    + NOT_NULL,
                "instructor"     to TEXT    + NOT_NULL,
                "user"           to INTEGER + NOT_NULL,
                "color"          to INTEGER + NOT_NULL,
                "location"       to TEXT)
    }
}

/**
 * Access property for Context
 */
val Context.database: DatabaseOpenHelper
    get() = DatabaseOpenHelper.getInstance(applicationContext)

fun SQLiteStatement.bindStringOrNull(index: Int, value: String?) {
    if (value != null) {
        this.bindString(index, value)
    } else {
        this.bindNull(index)
    }
}

fun String.escapeQuery() =
        this.replace("'", "''").replace("%", "\$%").replace("_", "\$_")

fun StringBuilder.appendIfNotEmpty(string: String): StringBuilder{
    if (this.isNotEmpty()) {
        this.append(string)
    }

    return this
}