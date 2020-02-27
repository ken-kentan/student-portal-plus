package jp.kentan.studentportalplus.ui.welcome.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class WelcomeNotificationViewModel @Inject constructor(
    private val localPreferences: LocalPreferences
) : ViewModel() {

    private val _startMainActivity = MutableLiveData<Event<Unit>>()
    val startMainActivity: LiveData<Event<Unit>>
        get() = _startMainActivity

    fun onCompleteClick() {
        _startMainActivity.value = Event(Unit)
    }
}
