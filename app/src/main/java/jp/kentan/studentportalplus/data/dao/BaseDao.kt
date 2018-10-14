package jp.kentan.studentportalplus.data.dao

import android.database.sqlite.SQLiteStatement

abstract class BaseDao {

    protected fun SQLiteStatement.bindStringOrNull(index: Int, value: String?) {
        if (value != null) {
            bindString(index, value)
        } else {
            bindNull(index)
        }
    }

    fun String.escapeQuery() =
            replace("'", "''").replace("%", "\$%").replace("_", "\$_")

    protected fun Boolean.toLong() = if (this) 1L else 0L
}