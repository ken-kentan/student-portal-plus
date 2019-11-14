package jp.kentan.studentportalplus.data.vo

import androidx.annotation.ColorRes
import jp.kentan.studentportalplus.R

enum class CourseColor(
    @ColorRes val resId: Int
) {
    LIGHT_BLUE_1(R.color.course_light_blue_1),
    LIGHT_BLUE_2(R.color.course_light_blue_2),
    LIGHT_BLUE_3(R.color.course_light_blue_3),
    LIGHT_BLUE_4(R.color.course_light_blue_4);

    companion object {
        val DEFAULT = LIGHT_BLUE_1
    }
}
