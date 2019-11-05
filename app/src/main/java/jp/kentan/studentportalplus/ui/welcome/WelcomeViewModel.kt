package jp.kentan.studentportalplus.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.ui.Event

class WelcomeViewModel : ViewModel() {

    val isAgreeChecked = MutableLiveData<Boolean>()

    private val _startLoginActivity = MutableLiveData<Event<Unit>>()
    val startLoginActivity: LiveData<Event<Unit>>
        get() = _startLoginActivity

    fun onShibbolethClick() {
        _startLoginActivity.value = Event(Unit)
    }
}