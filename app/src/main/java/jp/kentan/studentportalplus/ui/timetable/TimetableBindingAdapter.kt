package jp.kentan.studentportalplus.ui.timetable

import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.AttendCourse

object TimetableBindingAdapter {

    @JvmStatic
    @BindingAdapter("attendCourseListDayOfWeek")
    fun setListDayOfWeek(view: TextView, attendCourse: AttendCourse?) {
        if (attendCourse == null) {
            view.text = null
            return
        }

        view.text = view.context.getString(attendCourse.dayOfWeek.resId)

        if (attendCourse.dayOfWeek.hasPeriod) {
            view.append(attendCourse.period.toString())
        }
    }

    @JvmStatic
    @BindingAdapter("isTimetableTodayTextAppearance")
    fun setIsTimetableTodayTextAppearance(view: TextView, isToday: Boolean) {
        TextViewCompat.setTextAppearance(
            view,
            if (isToday) R.style.TextAppearance_AppTheme_Timetable_Today else R.style.TextAppearance_MaterialComponents_Body1
        )
    }
}
