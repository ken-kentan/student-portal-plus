package jp.kentan.studentportalplus.ui.welcome

import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter

object WelcomeBindingAdapter {

    @JvmStatic
    @BindingAdapter("url")
    fun setUrl(view: WebView, @StringRes urlResId: Int) {
        view.loadUrl(view.context.getString(urlResId))
    }
}
