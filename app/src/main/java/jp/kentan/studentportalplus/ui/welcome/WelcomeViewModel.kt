package jp.kentan.studentportalplus.ui.welcome

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.ui.SingleLiveData

class WelcomeViewModel : ViewModel() {

    val isCheckedAgree = ObservableBoolean()
    val startLoginActivity = SingleLiveData<Unit>()

    fun onClickShibboleth() {
        startLoginActivity.value = Unit
    }

    fun getWebViewClient() = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            view?.apply {
                if (!title.contains(context.getString(R.string.title_terms))) {
                    view.loadUrl(context.getString(R.string.url_terms_local))
                }
            }
        }
    }
}