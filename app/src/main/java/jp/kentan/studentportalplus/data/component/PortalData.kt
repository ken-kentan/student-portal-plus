package jp.kentan.studentportalplus.data.component

import androidx.annotation.StringRes
import jp.kentan.studentportalplus.R

enum class PortalData(
        val url: String,
        @StringRes val nameResId: Int
) {
    NOTICE("https://portal.student.kit.ac.jp", R.string.name_notice),
    LECTURE_INFO("https://portal.student.kit.ac.jp/ead/?c=lecture_information", R.string.name_lecture_info),
    LECTURE_CANCEL("https://portal.student.kit.ac.jp/ead/?c=lecture_cancellation", R.string.name_lecture_cancel),
    MY_CLASS("https://portal.student.kit.ac.jp/ead/?c=attend_course", R.string.name_my_class)
}