package jp.kentan.studentportalplus.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import jp.kentan.studentportalplus.R
import java.text.SimpleDateFormat
import java.util.*

private val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
fun Date.formatYearMonthDay(): String = DATE_FORMAT.format(this)

private val FULL_DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN)
fun Date.formatYearMonthDayHms(): String = FULL_DATE_FORMAT.format(this)

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

fun Context.buildCustomTabsIntent(): CustomTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .addDefaultShareMenuItem()
        .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        .build()