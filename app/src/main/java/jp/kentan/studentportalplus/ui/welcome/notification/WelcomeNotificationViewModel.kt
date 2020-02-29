package jp.kentan.studentportalplus.ui.welcome.notification

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class WelcomeNotificationViewModel @Inject constructor(
    context: Context,
    private val localPreferences: LocalPreferences
) : ViewModel() {

    val lectureNotificationTypeList: List<String> =
        context.resources.getStringArray(R.array.notification_type_lecture_name).asList()

    val noticeNotificationTypeList: List<String> =
        context.resources.getStringArray(R.array.notification_type_notice_name).asList()

    private val _startMainActivity = MutableLiveData<Event<Unit>>()
    val startMainActivity: LiveData<Event<Unit>>
        get() = _startMainActivity

    fun onCompleteClick() {
        _startMainActivity.value = Event(Unit)
    }
}
