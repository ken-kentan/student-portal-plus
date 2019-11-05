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

fun <T> MediatorLiveData<T>.asLiveData() = this as LiveData<T>

inline fun <T, E1, E2, R> LiveData<T>.combineWith(
    liveData1: LiveData<E1>,
    liveData2: LiveData<E2>,
    crossinline block: (T, E1, E2) -> R
): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) {
        result.value = block(
            it ?: return@addSource,
            liveData1.value ?: return@addSource,
            liveData2.value ?: return@addSource
        )
    }
    result.addSource(liveData1) {
        result.value = block(
            value ?: return@addSource,
            it ?: return@addSource,
            liveData2.value ?: return@addSource
        )
    }
    result.addSource(liveData2) {
        result.value = block(
            value ?: return@addSource,
            liveData1.value ?: return@addSource,
            it ?: return@addSource
        )
    }
    return result
}
