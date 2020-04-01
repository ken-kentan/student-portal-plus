package jp.kentan.studentportalplus.data.vo

import androidx.annotation.StringRes
import jp.kentan.studentportalplus.R

@Suppress("UNUSED")
enum class Period(
    val value: Int,
    @StringRes val startTimeResId: Int
) {
    ONE(1, R.string.period_1_start_time),
    TWO(2, R.string.period_2_start_time),
    THREE(3, R.string.period_3_start_time),
    FOUR(4, R.string.period_4_start_time),
    FIVE(5, R.string.period_5_start_time),
    SIX(6, R.string.period_6_start_time),
    SEVEN(7, R.string.period_7_start_time);

    companion object {
        val DEFAULT_RANGE = IntRange(1, 1)

        fun rangeOf(period: String): IntRange = with(period) {
            if (length >= 3) {
                val first = find { it.isDigit() } ?: return DEFAULT_RANGE
                val last = findLast { it.isDigit() } ?: return DEFAULT_RANGE

                IntRange(
                    start = Character.getNumericValue(first),
                    endInclusive = Character.getNumericValue(last)
                )
            } else {
                val p = find { it.isDigit() }?.let(Character::getNumericValue)
                    ?: return@with DEFAULT_RANGE

                IntRange(p, p)
            }
        }
    }
}
