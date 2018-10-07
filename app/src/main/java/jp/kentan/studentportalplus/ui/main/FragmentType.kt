package jp.kentan.studentportalplus.ui.main

import androidx.annotation.StringRes
import jp.kentan.studentportalplus.R

enum class FragmentType(
        @StringRes val titleResId: Int,
        val menuItemId: Int
) {
    DASHBOARD(
            R.string.title_fragment_dashboard,
            R.id.nav_dashboard),
    TIMETABLE(
            R.string.title_fragment_timetable,
            R.id.nav_timetable),
    LECTURE_INFO(
            R.string.title_fragment_lecture_info,
            R.id.nav_lecture_info),
    LECTURE_CANCEL(
            R.string.title_fragment_lecture_cancel,
            R.id.nav_lecture_cancel),
    NOTICE(
            R.string.title_fragment_notice,
            R.id.nav_notice)
}