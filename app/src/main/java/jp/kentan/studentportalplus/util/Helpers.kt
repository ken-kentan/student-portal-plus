package jp.kentan.studentportalplus.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
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

fun Boolean.toInt() = if (this) 1 else 0

fun Boolean.toLong() = if (this) 1L else 0L

fun Char.toIntOrNull() = toString().toIntOrNull()

/**
 * Convert Date to short String(yyyy/MM/dd)
 */
private val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)

fun Date.toShortString(): String? = DATE_FORMAT.format(this)

/**
 * Convert String to Spanned
 */
fun String.toSpanned(): Spanned{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(this)
    }
}