package jp.kentan.studentportalplus.util

import android.content.SharedPreferences

/**
 * SharedPreferencesHelper
 */

fun SharedPreferences.enableDetailErrorMessage() = getBoolean("enable_detail_error", false)

fun SharedPreferences.getMyClassThreshold() = (getString("my_class_threshold", "80").toIntOrNull() ?: 80) / 100f