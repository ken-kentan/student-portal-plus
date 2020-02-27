package jp.kentan.studentportalplus.ui.welcome.term

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.ui.Event

class WelcomeTermViewModel : ViewModel() {

    val isAgreeChecked = MutableLiveData<Boolean>()

    private val _termUrl = MutableLiveData<Int>(R.string.url_terms)
    val termUrl: LiveData<Int>
        get() = _termUrl

    private val _navigate = MutableLiveData<Event<Int>>()
    val navigate: LiveData<Event<Int>>
        get() = _navigate

    fun onWebViewReceivedError() {
        _termUrl.value = R.string.url_terms_local
    }

    fun onShibbolethClick() {
        _navigate.value = Event(R.id.login_fragment)
    }
}
