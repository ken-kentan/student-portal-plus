package jp.kentan.studentportalplus.ui.mycoursedetail

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.MyCourse

object MyCourseDetailBindingAdapter {

    @JvmStatic
    @BindingAdapter("myCourseDayOfWeek")
    fun setDayOfWeek(view: TextView, myCourse: MyCourse?) {
        if (myCourse == null) {
            view.text = null
            return
        }

        val context = view.context

        val period = if (myCourse.dayOfWeek.hasPeriod) context.getString(
            R.string.all_period_suffix, myCourse.period
        ) else ""

        val dayOfWeek = with(myCourse.dayOfWeek) {
            if (hasSuffix) {
                context.getString(
                    R.string.my_course_detail_day_of_week_suffix,
                    context.getString(resId)
                )
            } else {
                context.getString(resId)
            }
        }

        view.text = context.getString(
            R.string.my_course_detail_day_of_week_period,
            dayOfWeek,
            period
        )
    }
}
