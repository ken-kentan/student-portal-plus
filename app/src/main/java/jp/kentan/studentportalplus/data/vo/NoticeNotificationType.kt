package jp.kentan.studentportalplus.data.vo

import androidx.annotation.StringRes
import jp.kentan.studentportalplus.R

enum class NoticeNotificationType(
    @StringRes val resId: Int
) {
    ALL(R.string.name_notification_all),
    IMPORTANT(R.string.name_notification_important),
    NOT(R.string.name_notification_not)
}
