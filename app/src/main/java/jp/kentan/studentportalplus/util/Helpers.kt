package jp.kentan.studentportalplus.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat
import java.util.*


/**
 * Hide soft keyboard
 */
fun Activity.hideSoftInput() {
    try {
        val view = this.currentFocus ?: return

        val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!inputMethodManager.isActive) {
            return
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Boolean.toLong() = if (this) 1L else 0L

/**
 * convert Date to short String(yyyy/MM/dd)
 */
private val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)

fun Date.toShortString() = DATE_FORMAT.format(this)
