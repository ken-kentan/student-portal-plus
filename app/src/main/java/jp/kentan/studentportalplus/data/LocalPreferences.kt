package jp.kentan.studentportalplus.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import jp.kentan.studentportalplus.data.vo.LectureNotificationType
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.data.vo.NoticeNotificationType
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import java.util.*


class LocalPreferences(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val IS_AUTHENICATED_USER = "is_authenticated_user"

        private const val IS_GRID_TIMETABLE_LAYOUT = "is_grid_timetable_layout"
        private const val IS_ENABLED_PDF_OPEN_WITH_GDOCS = "is_enabled_pdf_open_with_gdocs"
        private const val IS_ENABLED_DETAIL_ERROR = "is_enabled_detail_error"
        private const val LECTURE_INFORMATIONS_ORDER = "lecture_informations_order"
        private const val LECTURE_CANCELLATIONS_ORDER = "lecture_cancellations_order"
        private const val SIMILAR_SUBJECT_THRESHOLD = "similar_subject_threshold"
        private const val SHIBBOLETH_LAST_LOGIN_DATE = "shibboleth_last_login_date"

        // For sync
        private const val IS_ENABLED_SYNC = "is_enabled_sync"
        private const val SYNC_INTERVAL_MINUTES = "sync_interval_minutes"

        // For notifications
        private const val IS_ENABLED_NOTIFICATION_VIBRATION = "is_enabled_notification_vibration"
        private const val IS_ENABLED_NOTIFICATION_LED = "is_enabled_notification_led"
        private const val NOTIFICATION_ID = "notification_id"
        private const val LECTURE_INFO_NOTIFICATION_TYPE = "notification_type_lecture_info"
        private const val LECTURE_CANCEL_NOTIFICATION_TYPE = "notification_type_lecture_cancel"
        private const val NOTICE_NOTIFICATION_TYPE = "notification_type_notice"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).apply {
        registerOnSharedPreferenceChangeListener(this@LocalPreferences)
    }

    val isEnabledSync: Boolean
        get() = sharedPreferences.getBoolean(IS_ENABLED_SYNC, true)

    var isAuthenticatedUser: Boolean
        get() = sharedPreferences.getBoolean(IS_AUTHENICATED_USER, false)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_AUTHENICATED_USER, value)
        }

    var isGridTimetableLayout: Boolean
        get() = sharedPreferences.getBoolean(IS_GRID_TIMETABLE_LAYOUT, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_GRID_TIMETABLE_LAYOUT, value)
        }

    var isEnabledPdfOpenWithGdocs: Boolean
        get() = sharedPreferences.getBoolean(IS_ENABLED_PDF_OPEN_WITH_GDOCS, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_ENABLED_PDF_OPEN_WITH_GDOCS, value)
        }

    var isEnabledDetailError: Boolean
        get() = sharedPreferences.getBoolean(IS_ENABLED_DETAIL_ERROR, false)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_ENABLED_DETAIL_ERROR, value)
        }

    var isEnabledNotificationVibration: Boolean
        get() = sharedPreferences.getBoolean(IS_ENABLED_NOTIFICATION_VIBRATION, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_ENABLED_NOTIFICATION_VIBRATION, value)
        }

    var isEnabledNotificationLed: Boolean
        get() = sharedPreferences.getBoolean(IS_ENABLED_NOTIFICATION_LED, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_ENABLED_NOTIFICATION_LED, value)
        }

    var lectureInformationsOrder: LectureQuery.Order
        get() = LectureQuery.Order.valueOf(
            sharedPreferences.getString(
                LECTURE_INFORMATIONS_ORDER, null
            ) ?: LectureQuery.Order.UPDATED_DATE.name
        )
        set(value) = sharedPreferences.edit {
            putString(LECTURE_INFORMATIONS_ORDER, value.name)
        }

    var lectureCancellationsOrder: LectureQuery.Order
        get() = LectureQuery.Order.valueOf(
            sharedPreferences.getString(
                LECTURE_CANCELLATIONS_ORDER, null
            ) ?: LectureQuery.Order.UPDATED_DATE.name
        )
        set(value) = sharedPreferences.edit {
            putString(LECTURE_CANCELLATIONS_ORDER, value.name)
        }

    val similarSubjectThreshold: Float
        get() = (sharedPreferences.getString(SIMILAR_SUBJECT_THRESHOLD, null)?.toIntOrNull()
            ?: 80) / 100F

    private val similarSubjectThresholdChannel =
        ConflatedBroadcastChannel(similarSubjectThreshold)
    val similarSubjectThresholdFlow = similarSubjectThresholdChannel.asFlow()

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == SIMILAR_SUBJECT_THRESHOLD) {
            similarSubjectThresholdChannel.offer(similarSubjectThreshold)
        }
    }

    val shibbolethLastLoginDate: Date
        get() = Date(sharedPreferences.getLong(SHIBBOLETH_LAST_LOGIN_DATE, 0))

    fun updateShibbolethLastLoginDate() {
        sharedPreferences.edit {
            putLong(SHIBBOLETH_LAST_LOGIN_DATE, System.currentTimeMillis())
        }
    }

    val syncIntervalMinutes: Long
        get() = sharedPreferences.getString("sync_interval_minutes", "120")?.toLongOrNull()
            ?: 120L

    var notificationId: Int
        get() = sharedPreferences.getInt(NOTIFICATION_ID, 1)
        set(value) = sharedPreferences.edit {
            putInt(NOTIFICATION_ID, value)
        }

    var lectureInformationNotificationType: LectureNotificationType
        get() {
            val type = sharedPreferences.getString(LECTURE_INFO_NOTIFICATION_TYPE, null)
                ?: return LectureNotificationType.ALL
            return LectureNotificationType.valueOf(type)
        }
        set(value) = sharedPreferences.edit {
            putString(LECTURE_INFO_NOTIFICATION_TYPE, value.name)
        }

    var lectureCancellationNotificationType: LectureNotificationType
        get() {
            val type = sharedPreferences.getString(LECTURE_CANCEL_NOTIFICATION_TYPE, null)
                ?: return LectureNotificationType.ALL
            return LectureNotificationType.valueOf(type)
        }
        set(value) = sharedPreferences.edit {
            putString(LECTURE_CANCEL_NOTIFICATION_TYPE, value.name)
        }

    var noticeNotificationType: NoticeNotificationType
        get() {
            val type = sharedPreferences.getString(NOTICE_NOTIFICATION_TYPE, null)
                ?: return NoticeNotificationType.ALL
            return NoticeNotificationType.valueOf(type)
        }
        set(value) = sharedPreferences.edit {
            putString(NOTICE_NOTIFICATION_TYPE, value.name)
        }
}
