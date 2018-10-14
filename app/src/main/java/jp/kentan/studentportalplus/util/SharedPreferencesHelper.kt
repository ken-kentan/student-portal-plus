package jp.kentan.studentportalplus.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.component.PortalData
import jp.kentan.studentportalplus.notification.NotificationType
import org.jetbrains.anko.defaultSharedPreferences
import java.util.*

fun SharedPreferences.isAuthenticatedUser() = getBoolean("is_authenticated_user", false)

fun SharedPreferences.isEnabledDetailError() = getBoolean("is_enabled_detail_error", false)

fun SharedPreferences.isEnabledPdfOpenWithGdocs() = getBoolean("is_enabled_pdf_open_with_gdocs", true)

fun SharedPreferences.isGridTimetableLayout() = getBoolean("is_grid_timetable_layout", true)

fun SharedPreferences.isEnabledSync() = getBoolean("is_enabled_sync", true)

fun SharedPreferences.isEnabledNotificationVibration() = getBoolean("is_enabled_notification_vibration", true)

fun SharedPreferences.isEnabledNotificationLed() = getBoolean("is_enabled_notification_led", true)

fun SharedPreferences.getShibbolethLastLoginDate() = getLong("shibboleth_last_login_date", -1)

fun SharedPreferences.getLectureInfoOrder() =
        LectureQuery.Order.values()[getInt("lecture_info_order", 0)]

fun SharedPreferences.getLectureCancelOrder() =
        LectureQuery.Order.values()[getInt("lecture_cancel_order", 0)]

fun SharedPreferences.getSyncIntervalMinutes() = getString("sync_interval_minutes", "120")?.toLongOrNull() ?: 120L

fun SharedPreferences.getSimilarSubjectThreshold() = getString("similar_subject_threshold", "80")?.toIntOrNull() ?: 80

fun SharedPreferences.getSimilarSubjectThresholdFloat() = getSimilarSubjectThreshold() / 100f

fun SharedPreferences.getNotificationId() = getInt("notification_id", 1)

fun SharedPreferences.getNotificationType(type: PortalData): NotificationType {
    val key = when (type) {
        PortalData.NOTICE -> "notification_type_notice"
        PortalData.LECTURE_INFO -> "notification_type_lecture_info"
        PortalData.LECTURE_CANCEL -> "notification_type_lecture_cancel"
        PortalData.MY_CLASS -> "notification_type_my_class"
    }

    return NotificationType.valueOf(getString(key, null) ?: NotificationType.ALL.name)
}

fun SharedPreferences.setAuthenticatedUser(isAuthenticated: Boolean) = edit { putBoolean("is_authenticated_user", isAuthenticated) }

fun SharedPreferences.setGridTimetableLayout(isGrid: Boolean) = edit { putBoolean("is_grid_timetable_layout", isGrid) }

fun SharedPreferences.setLectureInfoOrder(order: LectureQuery.Order) = edit { putInt("lecture_info_order", order.ordinal) }

fun SharedPreferences.setLectureCancelOrder(order: LectureQuery.Order) = edit { putInt("lecture_cancel_order", order.ordinal) }

fun SharedPreferences.setNotificationId(id: Int) = edit { putInt("notification_id", id) }

fun Context.updateShibbolethLastLoginDate() {
    defaultSharedPreferences.edit {
        putLong("shibboleth_last_login_date", Date().time)
    }
}