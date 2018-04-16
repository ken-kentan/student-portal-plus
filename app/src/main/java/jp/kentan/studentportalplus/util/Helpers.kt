package jp.kentan.studentportalplus.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.design.widget.Snackbar
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

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

fun indefiniteSnackbar(view: View, message: String, actionMessage: String) {
    val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
    snackbar.setAction(actionMessage, { snackbar.dismiss() })
    snackbar.show()
}

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
    var htmlStr = this

    htmlTags.forEach { (pattern, span) ->
        htmlStr = pattern.matcher(htmlStr).replaceAll(span)
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlStr, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(htmlStr)
    }
}

/**
 * Support custom SPAN class
 * https://portal.student.kit.ac.jp/css/common/wb_common.css
 */
private val htmlTags by lazy {
    mapOf<Pattern, String>(
            Pattern.compile("<span class=\"col_red\">(.*?)</span>") to "<font color=\"#ff0000\">\$1</font>",
            Pattern.compile("<span class=\"col_green\">(.*?)</span>") to "<font color=\"#008000\">\$1</font>",
            Pattern.compile("<span class=\"col_blue\">(.*?)</span>") to "<font color=\"#0000ff\">\$1</font>",
            Pattern.compile("<span class=\"col_orange\">(.*?)</span>") to "<font color=\"#ffa500\">\$1</font>",
            Pattern.compile("<span class=\"col_white\">(.*?)</span>") to "<font color=\"#ffffff\">\$1</font>",
            Pattern.compile("<span class=\"col_black\">(.*?)</span>") to "<font color=\"#000000\">\$1</font>",
            Pattern.compile("<span class=\"col_gray\">(.*?)</span>") to "<font color=\"#999999\">\$1</font>",
            Pattern.compile("<a href=\"(.*?)\"(.*?)\">(.*?)</a>") to "\$3( \$1 )",
            Pattern.compile("<span class=\"u_line\">(.*?)</span>") to "<u>\$1</u>",
            Pattern.compile("([A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}?)") to " \$1 "
    )
}