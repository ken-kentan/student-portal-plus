package jp.kentan.studentportalplus.util

import android.content.SharedPreferences

/**
 * SharedPreferencesHelper
 */

fun SharedPreferences.enableDetailErrorMessage() = getBoolean("enable_detail_error", false)