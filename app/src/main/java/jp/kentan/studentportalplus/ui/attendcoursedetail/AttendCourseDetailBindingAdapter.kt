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

        val period = with(attendCourse.period) {
            if (this > 0) context.getString(
                R.string.suffix_period, this
            ) else ""
        }
        val dayOfWeek = with(attendCourse.dayOfWeek) {
            if (hasSuffix) {
                context.getString(R.string.suffix_day_of_week_short, context.getString(resId))
            } else {
                context.getString(resId)
            }
        }

        view.text = context.getString(
            R.string.text_day_of_week_and_period,
            dayOfWeek,
            period
        )
    }
}