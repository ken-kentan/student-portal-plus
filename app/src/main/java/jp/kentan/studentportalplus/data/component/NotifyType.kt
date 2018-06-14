package jp.kentan.studentportalplus.data.component

import android.content.SharedPreferences

enum class NotifyType(
        val displayName: String
) {
    ALL("すべて"),
    ATTEND("受講科目・類似科目"),
    NOT("通知しない");

    companion object {
        fun getBy(preferences: SharedPreferences, key: String): NotifyType {
            return valueOf(preferences.getString(key, ALL.name))
        }
    }
}