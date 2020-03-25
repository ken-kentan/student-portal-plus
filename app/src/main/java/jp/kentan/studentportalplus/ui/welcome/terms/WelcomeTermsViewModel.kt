package jp.kentan.studentportalplus.ui.welcome.terms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.ui.Event

class WelcomeTermsViewModel : ViewModel() {

    val isAgreeChecked = MutableLiveData<Boolean>()

    private val _termUrl = MutableLiveData(R.string.all_terms_url)
    val termUrl: LiveData<Int>
        get() = _termUrl

    private val _navigateToLogin = MutableLiveData<Event<Unit>>()
    val navigateToLogin: LiveData<Event<Unit>>
        get() = _navigateToLogin

    fun onWebViewReceivedError() {
        _termUrl.value = R.string.all_terms_local_url
    }

    fun onShibbolethClick() {
        _navigateToLogin.value = Event(Unit)
    }
}
