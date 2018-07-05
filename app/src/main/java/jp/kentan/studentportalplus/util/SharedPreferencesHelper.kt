package jp.kentan.studentportalplus.util

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * SharedPreferencesHelper
 */

fun SharedPreferences.enabledDetailError() = getBoolean("enabled_detail_error", false)

fun SharedPreferences.enabledPdfOpenWithGdocs() = getBoolean("enabled_pdf_open_with_gdocs", false)

fun SharedPreferences.isFirstLaunch() = getBoolean("is_first_launch", true)

fun SharedPreferences.setFirstLaunch(isFirst: Boolean) = edit { putBoolean("is_first_launch", isFirst) }

fun SharedPreferences.enabledSync() = getBoolean("enabled_sync", true)

fun SharedPreferences.getMyClassThreshold() = (getString("my_class_threshold", "80").toIntOrNull() ?: 80) / 100f

fun SharedPreferences.getSyncIntervalMinutes() = getString("sync_interval", "60").toLongOrNull() ?: 60L

// For notification
fun SharedPreferences.enabledNotificationVibration() = getBoolean("enabled_notification_vibration", true)
fun SharedPreferences.enabledNotificationLed() = getBoolean("enabled_notification_led", true)

fun SharedPreferences.getNotificationId() = getInt("notification_id", 1)
fun SharedPreferences.setNotificationId(id: Int) = edit { putInt("notification_id", id) }