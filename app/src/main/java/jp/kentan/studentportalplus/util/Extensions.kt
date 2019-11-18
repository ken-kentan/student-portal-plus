package jp.kentan.studentportalplus.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import jp.kentan.studentportalplus.R
import java.text.SimpleDateFormat
import java.util.*

fun Activity.hideSoftInput() {
    try {
        val view = this.currentFocus ?: return

        val inputMethodManager =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
    .setToolbarColor(ContextCompat.getColor(this, R.color.primary))
    .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.primary_variant))
    .build()

inline fun <T : ViewDataBinding> T.executeAfter(block: T.() -> Unit) {
    block()
    executePendingBindings()
}

private val YMD_DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
fun Date.formatYearMonthDay(): String = YMD_DATE_FORMAT.format(this)

fun String.isPdf() = endsWith(".pdf", ignoreCase = true)

fun <T> MediatorLiveData<T>.asLiveData() = this as LiveData<T>
