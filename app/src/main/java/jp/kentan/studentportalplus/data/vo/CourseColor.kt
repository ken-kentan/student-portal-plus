package jp.kentan.studentportalplus.data.vo

import androidx.annotation.ColorRes
import jp.kentan.studentportalplus.R

@Suppress("UNUSED")
enum class CourseColor(
    @ColorRes val resId: Int
) {
    LIGHT_BLUE_1(R.color.course_light_blue_1),
    LIGHT_BLUE_2(R.color.course_light_blue_2),
    LIGHT_BLUE_3(R.color.course_light_blue_3),
    LIGHT_BLUE_4(R.color.course_light_blue_4),
    GREEN_4(R.color.course_green_4),
    GREEN_3(R.color.course_green_3),
    GREEN_2(R.color.course_green_2),
    GREEN_1(R.color.course_green_1),
    RED_1(R.color.course_red_1),
    RED_2(R.color.course_red_2),
    RED_3(R.color.course_red_3),
    RED_4(R.color.course_red_4),
    ORANGE_4(R.color.course_orange_4),
    ORANGE_3(R.color.course_orange_3),
    ORANGE_2(R.color.course_orange_2),
    ORANGE_1(R.color.course_orange_1),
    PURPLE_1(R.color.course_purple_1),
    PURPLE_2(R.color.course_purple_2),
    PURPLE_3(R.color.course_purple_3),
    PURPLE_4(R.color.course_purple_4),
    BROWN_4(R.color.course_brown_4),
    BROWN_3(R.color.course_brown_3),
    BROWN_2(R.color.course_brown_2),
    BROWN_1(R.color.course_brown_1);

    companion object {
        val DEFAULT = LIGHT_BLUE_1
    }
}
