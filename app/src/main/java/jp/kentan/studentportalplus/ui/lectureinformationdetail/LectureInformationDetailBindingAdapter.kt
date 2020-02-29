package jp.kentan.studentportalplus.ui.lectureinformationdetail

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.util.formatYearMonthDay

object LectureInformationDetailBindingAdapter {

    @JvmStatic
    @BindingAdapter("lectureInfoSemester")
    fun setSemester(view: TextView, lectureInfo: LectureInformation?) {
        if (lectureInfo == null) {
            view.text = null
            return
        }

        val semester: String = lectureInfo.semester.let {
            if (arrayOf("前", "後", "春", "秋").contains(it)) "${it}学期" else it
        }
        val period: String = lectureInfo.period.let {
            if (it != "-") "${it}限" else ""
        }

        view.text = view.context.getString(
            R.string.lecture_information_detail_semester_day_of_week_period,
            lectureInfo.grade,
            semester,
            lectureInfo.dayOfWeek.replace("曜日", "曜"),
            period
        )
    }

    @JvmStatic
    @BindingAdapter("lectureInfoDate")
    fun setDate(view: TextView, data: LectureInformation?) {
        if (data == null) {
            view.text = null
            return
        }

        val context = view.context

        view.text = context.getString(
            R.string.lecture_information_detail_created_date,
            data.createdDate.formatYearMonthDay()
        )

        if (data.createdDate != data.updatedDate) {
            view.append(
                context.getString(
                    R.string.lecture_information_detail_updated_date,
                    data.updatedDate.formatYearMonthDay()
                )
            )
        }
    }
}
