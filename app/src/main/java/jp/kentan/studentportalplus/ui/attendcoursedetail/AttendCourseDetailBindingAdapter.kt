package jp.kentan.studentportalplus.ui.attendcoursedetail

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.AttendCourse

object AttendCourseDetailBindingAdapter {
    @JvmStatic
    @BindingAdapter("attendCourseDayOfWeek")
    fun setDayOfWeek(view: TextView, attendCourse: AttendCourse?) {
        if (attendCourse == null) {
            view.text = null
            return
        }

        val context = view.context

        val period = if (attendCourse.dayOfWeek.hasPeriod) context.getString(
            R.string.all_period_suffix, attendCourse.period
        ) else ""

        val dayOfWeek = with(attendCourse.dayOfWeek) {
            if (hasSuffix) {
                context.getString(
                    R.string.attend_course_detail_day_of_week_suffix,
                    context.getString(resId)
                )
            } else {
                context.getString(resId)
            }
        }

        view.text = context.getString(
            R.string.attend_course_detail_day_of_week_period,
            dayOfWeek,
            period
        )
    }
}
