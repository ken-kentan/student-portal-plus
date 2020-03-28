package jp.kentan.studentportalplus.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import jp.kentan.studentportalplus.data.vo.LectureNotificationType
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.data.vo.NoticeNotificationType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.util.Date

interface Preferences {

    var isAuthenticatedUser: Boolean

    val isSyncEnabled: Boolean
    val syncIntervalMinutes: Long

    var isGridTimetableLayout: Boolean

    var isPdfOpenWithGdocsEnabled: Boolean

    var isDetailErrorEnabled: Boolean

    var lectureInformationsOrder: LectureQuery.Order
    var lectureCancellationsOrder: LectureQuery.Order

    val similarSubjectThreshold: Float
    val similarSubjectThresholdFlow: Flow<Float>

    val shibbolethLastLoginDate: Date

    var notificationId: Int

    var isNotificationVibrationEnabled: Boolean
    var isNotificationLedEnabled: Boolean

    var lectureInformationNotificationType: LectureNotificationType
    var lectureCancellationNotificationType: LectureNotificationType
    var noticeNotificationType: NoticeNotificationType

    fun updateShibbolethLastLoginDate()
}

@FlowPreview
@ExperimentalCoroutinesApi
class LocalPreferences(
    context: Context
) : Preferences, SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val PREFERENCES_VERSION = "preferences_version"

        private const val IS_AUTHENTICATED_USER = "is_authenticated_user"

        private const val IS_SYNC_ENABLED = "is_sync_enabled"
        private const val SYNC_INTERVAL_MINUTES = "sync_interval_minutes"

        private const val IS_GRID_TIMETABLE_LAYOUT = "is_grid_timetable_layout"

        private const val IS_PDF_OPEN_WITH_GDOCS_ENABLED = "is_pdf_open_with_gdocs_enabled"

        private const val IS_DETAIL_ERROR_ENABLED = "is_detail_error_enabled"

        private const val LECTURE_INFORMATIONS_ORDER = "lecture_informations_order"
        private const val LECTURE_CANCELLATIONS_ORDER = "lecture_cancellations_order"

        private const val SIMILAR_SUBJECT_THRESHOLD = "similar_subject_threshold"

        private const val SHIBBOLETH_LAST_LOGIN_DATE = "shibboleth_last_login_date"

        private const val NOTIFICATION_ID = "notification_id"

        private const val IS_NOTIFICATION_VIBRATION_ENABLED = "is_notification_vibration_enabled"
        private const val IS_NOTIFICATION_LED_ENABLED = "is_notification_led_enabled"

        private const val LECTURE_INFO_NOTIFICATION_TYPE = "notification_type_lecture_info"
        private const val LECTURE_CANCEL_NOTIFICATION_TYPE = "notification_type_lecture_cancel"
        private const val NOTICE_NOTIFICATION_TYPE = "notification_type_notice"

        private const val DEFAULT_SYNC_INTERVAL_MINUTES = 120L
        private const val DEFAULT_SIMILAR_SUBJECT_THRESHOLD = 80
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).apply {
        registerOnSharedPreferenceChangeListener(this@LocalPreferences)
    }

    private val similarSubjectThresholdChannel =
        ConflatedBroadcastChannel(similarSubjectThreshold)

    override var isAuthenticatedUser: Boolean
        get() = sharedPreferences.getBoolean(IS_AUTHENTICATED_USER, false)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_AUTHENTICATED_USER, value)
        }

    override val isSyncEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_SYNC_ENABLED, true)

    override val syncIntervalMinutes: Long
        get() = sharedPreferences.getString(SYNC_INTERVAL_MINUTES, null)?.toLongOrNull()
            ?: DEFAULT_SYNC_INTERVAL_MINUTES

    override var isGridTimetableLayout: Boolean
        get() = sharedPreferences.getBoolean(IS_GRID_TIMETABLE_LAYOUT, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_GRID_TIMETABLE_LAYOUT, value)
        }

    override var isPdfOpenWithGdocsEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_PDF_OPEN_WITH_GDOCS_ENABLED, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_PDF_OPEN_WITH_GDOCS_ENABLED, value)
        }

    override var isDetailErrorEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_DETAIL_ERROR_ENABLED, false)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_DETAIL_ERROR_ENABLED, value)
        }

    override var lectureInformationsOrder: LectureQuery.Order
        get() = sharedPreferences.getString(LECTURE_INFORMATIONS_ORDER, null)
            ?.let(LectureQuery.Order::valueOf) ?: LectureQuery.Order.UPDATED_DATE
        set(value) = sharedPreferences.edit {
            putString(LECTURE_INFORMATIONS_ORDER, value.name)
        }

    override var lectureCancellationsOrder: LectureQuery.Order
        get() = sharedPreferences.getString(LECTURE_CANCELLATIONS_ORDER, null)
            ?.let(LectureQuery.Order::valueOf) ?: LectureQuery.Order.UPDATED_DATE
        set(value) = sharedPreferences.edit {
            putString(LECTURE_CANCELLATIONS_ORDER, value.name)
        }

    override val similarSubjectThreshold: Float
        get() = (sharedPreferences.getString(SIMILAR_SUBJECT_THRESHOLD, null)
            ?.toIntOrNull() ?: DEFAULT_SIMILAR_SUBJECT_THRESHOLD) / 100F

    override val similarSubjectThresholdFlow: Flow<Float>
        get() = similarSubjectThresholdChannel.asFlow()

    override val shibbolethLastLoginDate: Date
        get() = Date(sharedPreferences.getLong(SHIBBOLETH_LAST_LOGIN_DATE, 0))

    override var notificationId: Int
        get() = sharedPreferences.getInt(NOTIFICATION_ID, 1)
        set(value) = sharedPreferences.edit {
            putInt(NOTIFICATION_ID, value)
        }

    override var isNotificationVibrationEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_NOTIFICATION_VIBRATION_ENABLED, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_NOTIFICATION_VIBRATION_ENABLED, value)
        }

    override var isNotificationLedEnabled: Boolean
        get() = sharedPreferences.getBoolean(IS_NOTIFICATION_LED_ENABLED, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_NOTIFICATION_LED_ENABLED, value)
        }

    override var lectureInformationNotificationType: LectureNotificationType
        get() = sharedPreferences.getString(LECTURE_INFO_NOTIFICATION_TYPE, null)
            ?.let(LectureNotificationType::valueOf) ?: LectureNotificationType.ALL
        set(value) = sharedPreferences.edit {
            putString(LECTURE_INFO_NOTIFICATION_TYPE, value.name)
        }

    override var lectureCancellationNotificationType: LectureNotificationType
        get() = sharedPreferences.getString(LECTURE_CANCEL_NOTIFICATION_TYPE, null)
            ?.let(LectureNotificationType::valueOf) ?: LectureNotificationType.ALL
        set(value) = sharedPreferences.edit {
            putString(LECTURE_CANCEL_NOTIFICATION_TYPE, value.name)
        }

    override var noticeNotificationType: NoticeNotificationType
        get() = sharedPreferences.getString(NOTICE_NOTIFICATION_TYPE, null)
            ?.let(NoticeNotificationType::valueOf) ?: NoticeNotificationType.ALL
        set(value) = sharedPreferences.edit {
            putString(NOTICE_NOTIFICATION_TYPE, value.name)
        }

    init {
        if (sharedPreferences.getInt(PREFERENCES_VERSION, 0) == 0) {
            sharedPreferences.edit {
                clear()
                putInt(PREFERENCES_VERSION, 1)
            }
        }
    }

    override fun updateShibbolethLastLoginDate() {
        sharedPreferences.edit {
            putLong(SHIBBOLETH_LAST_LOGIN_DATE, System.currentTimeMillis())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == SIMILAR_SUBJECT_THRESHOLD) {
            similarSubjectThresholdChannel.offer(similarSubjectThreshold)
        }
    }
}
