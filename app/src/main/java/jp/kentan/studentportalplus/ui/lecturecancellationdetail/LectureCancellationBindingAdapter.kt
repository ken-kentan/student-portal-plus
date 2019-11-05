package jp.kentan.studentportalplus.ui.lecturecancellationdetail

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.util.formatYearMonthDay

object LectureCancellationBindingAdapter {
    @JvmStatic
    @BindingAdapter("lectureCancelGrade")
    fun setGrade(view: TextView, lectureCancel: LectureCancellation?) {
        if (lectureCancel == null) {
            view.text = null
            return
        }

        val period: String = lectureCancel.period.let {
            if (it != "-") "${it}限" else ""
        }

        view.text = view.context.getString(
            R.string.text_grade_day_of_week_period,
            lectureCancel.grade,
            lectureCancel.dayOfWeek.replace("曜日", "曜"),
            period
        )
    }

    @JvmStatic
    @BindingAdapter("lectureCancelDate")
    fun setDate(view: TextView, lectureCancel: LectureCancellation?) {
        if (lectureCancel == null) {
            view.text = null
            return
        }

        view.text = view.context.getString(
            R.string.text_lecture_cancellation_created_date,
            lectureCancel.createdDate.formatYearMonthDay()
        )
    }
}