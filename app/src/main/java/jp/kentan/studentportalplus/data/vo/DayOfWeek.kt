package jp.kentan.studentportalplus.data.vo

import androidx.annotation.StringRes
import jp.kentan.studentportalplus.R

enum class DayOfWeek(
    @StringRes val resId: Int,
    val hasSuffix: Boolean = true,
    val hasPeriod: Boolean = true
) {
    MONDAY(R.string.name_week_monday),
    TUESDAY(R.string.name_week_tuesday),
    WEDNESDAY(R.string.name_week_wednesday),
    THURSDAY(R.string.name_week_thursday),
    FRIDAY(R.string.name_week_friday),
    SATURDAY(R.string.name_week_saturday),
    SUNDAY(R.string.name_week_sunday),
    INTENSIVE(R.string.name_week_intensive, false, false),
    UNKNOWN(R.string.name_week_unknown, false, false);

    companion object {
        val WEEKDAY = setOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

        fun similarOf(name: String) = when {
            name.startsWith("月") -> MONDAY
            name.startsWith("火") -> TUESDAY
            name.startsWith("水") -> WEDNESDAY
            name.startsWith("木") -> THURSDAY
            name.startsWith("金") -> FRIDAY
            name.startsWith("土") -> SATURDAY
            name.startsWith("日") -> SUNDAY
            name == "集中" -> INTENSIVE
            name == "-" -> UNKNOWN
            else -> null
        }
    }
}