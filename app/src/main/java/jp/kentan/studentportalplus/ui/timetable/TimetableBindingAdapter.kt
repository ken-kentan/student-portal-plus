package jp.kentan.studentportalplus.ui.timetable

import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.MyCourse

object TimetableBindingAdapter {

    @JvmStatic
    @BindingAdapter("myCourseListDayOfWeek")
    fun setListDayOfWeek(view: TextView, myCourse: MyCourse?) {
        if (myCourse == null) {
            view.text = null
            return
        }

        view.text = view.context.getString(myCourse.dayOfWeek.resId)

        if (myCourse.dayOfWeek.hasPeriod) {
            view.append(myCourse.period.toString())
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
