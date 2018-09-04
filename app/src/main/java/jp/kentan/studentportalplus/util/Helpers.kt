package jp.kentan.studentportalplus.util

import android.app.Activity
import android.content.Context
import android.support.design.widget.Snackbar
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.text.parseAsHtml
import jp.kentan.studentportalplus.ui.span.CustomTitle
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

var Activity.customTitle: String
    set(title) {
        setTitle(CustomTitle(this, title))
    }
    get() = title.toString()

fun indefiniteSnackbar(view: View, message: String, actionMessage: String) {
    val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
    snackbar.setAction(actionMessage) { snackbar.dismiss() }
    snackbar.show()
}

fun Boolean.toLong() = if (this) 1L else 0L

fun Char.toIntOrNull() = toString().toIntOrNull()

fun String?.trimOrEmpty() = this?.trim() ?: ""

/**
 * Convert Date to short String(yyyy/MM/dd)
 */
private val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)

@Deprecated("use Date.formatToYearMonthDay()")
fun Date.toShortString(): String? = DATE_FORMAT.format(this)

fun Date.formatToYearMonthDay(): String = DATE_FORMAT.format(this)

/**
 * Convert String to Spanned
 */
fun String.htmlToSpanned(): Spanned{
    var html = this

    HTML_TAGS.forEach { (pattern, span) ->
        html = pattern.matcher(html).replaceAll(span)
    }

    return html.parseAsHtml()
}

/**
 * Support custom SPAN class
 * https://portal.student.kit.ac.jp/css/common/wb_common.css
 */
private val HTML_TAGS by lazy {
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