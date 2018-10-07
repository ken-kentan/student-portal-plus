package jp.kentan.studentportalplus.data.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

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
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTablesIfNotExist(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 2 && newVersion == 3) {
            DatabaseMigrationHelper.upgradeVersion3From2(db, ::createTablesIfNotExist)
        }
    }

    private fun createTablesIfNotExist(db: SQLiteDatabase) {
        db.createTable(NoticeDao.TABLE_NAME, true,
                "_id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "hash"         to INTEGER + NOT_NULL + UNIQUE,
                "created_date" to INTEGER + NOT_NULL,
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
                "created_date" to INTEGER + NOT_NULL,
                "updated_date" to INTEGER + NOT_NULL,
                "read"         to INTEGER + NOT_NULL + DEFAULT("0"))

        db.createTable(LectureCancellationDao.TABLE_NAME, true,
                "_id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "hash"         to INTEGER + NOT_NULL + UNIQUE,
                "grade"        to TEXT + NOT_NULL,
                "subject"      to TEXT + NOT_NULL,
                "instructor"   to TEXT + NOT_NULL,
                "cancel_date"  to INTEGER + NOT_NULL,
                "week"         to TEXT + NOT_NULL,
                "period"       to TEXT + NOT_NULL,
                "detail_text"  to TEXT + NOT_NULL,
                "detail_html"  to TEXT + NOT_NULL,
                "created_date" to INTEGER + NOT_NULL,
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
