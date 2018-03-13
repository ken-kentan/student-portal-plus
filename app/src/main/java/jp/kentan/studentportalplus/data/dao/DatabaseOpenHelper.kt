package jp.kentan.studentportalplus.data.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.util.Log
import org.jetbrains.anko.db.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * DatabaseOpenHelper
 *
 * https://github.com/Kotlin/anko/wiki/Anko-SQLite
 */
class DatabaseOpenHelper(context: Context) : ManagedSQLiteOpenHelper(context, "portal_data.db", null, version = 1) {

    companion object {
        private const val TAG = "DatabaseOpenHelper"

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
        // Create notice table
        db.createTable(NoticeDao.TABLE_NAME, true,
                "_id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "hash"         to INTEGER + NOT_NULL + UNIQUE,
                "created_date" to TEXT + NOT_NULL,
                "in_charge"    to TEXT + NOT_NULL,
                "category"     to TEXT + NOT_NULL,
                "title"        to TEXT + NOT_NULL,
                "detail"       to TEXT,
                "link"         to TEXT,
                "read"         to INTEGER + DEFAULT("0"),
                "favorite"     to INTEGER + DEFAULT("0"))

        Log.d(TAG, "Created notice table")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
//        db.dropTable("User", true)
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