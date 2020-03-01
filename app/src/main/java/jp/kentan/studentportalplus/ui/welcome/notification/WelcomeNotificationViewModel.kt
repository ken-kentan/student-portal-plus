package jp.kentan.studentportalplus.ui.welcome.notification

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.data.vo.LectureNotificationType
import jp.kentan.studentportalplus.data.vo.NoticeNotificationType
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

    val lectureInformationNotificationType = MutableLiveData<String>()
    val lectureCancellationNotificationType = MutableLiveData<String>()
    val noticeNotificationType = MutableLiveData<String>()

    private val _startMainActivity = MutableLiveData<Event<Unit>>()
    val startMainActivity: LiveData<Event<Unit>>
        get() = _startMainActivity

    private val lectureNotificationTypeMap: Map<String, LectureNotificationType> =
        lectureNotificationTypeList.zip(LectureNotificationType.values()).toMap()

    private val noticeNotificationTypeMap: Map<String, NoticeNotificationType> =
        noticeNotificationTypeList.zip(NoticeNotificationType.values()).toMap()

    init {
        lectureInformationNotificationType.value = lectureNotificationTypeMap.entries
            .first { entry ->
                entry.value == localPreferences.lectureInformationNotificationType
            }.key

        lectureCancellationNotificationType.value = lectureNotificationTypeMap.entries
            .first { entry ->
                entry.value == localPreferences.lectureCancellationNotificationType
            }.key

        noticeNotificationType.value = noticeNotificationTypeMap.entries
            .first { entry ->
                entry.value == localPreferences.noticeNotificationType
            }.key
    }

    fun onCompleteClick() {
        localPreferences.lectureInformationNotificationType =
            lectureNotificationTypeMap[lectureInformationNotificationType.value]
                ?: throw IllegalStateException("Invalid lecture info notification type name")

        localPreferences.lectureCancellationNotificationType =
            lectureNotificationTypeMap[lectureCancellationNotificationType.value]
                ?: throw IllegalStateException("Invalid lecture cancel notification type name")

        localPreferences.noticeNotificationType =
            noticeNotificationTypeMap[noticeNotificationType.value]
                ?: throw IllegalStateException("Invalid notice notification type name")

        _startMainActivity.value = Event(Unit)
    }
}
