package jp.kentan.studentportalplus.util

import android.databinding.BindingAdapter
import android.graphics.Typeface
import android.widget.ImageView
import android.widget.TextView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.component.LectureAttendType
import java.util.*

@BindingAdapter("text")
fun setText(textView: TextView, text: String?) {
    textView.text = if (text.isNullOrBlank()) "未入力" else text
}

@BindingAdapter("isRead")
fun setIsRead(textView: TextView, isRead: Boolean) {
    textView.typeface = if (isRead) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
}

@BindingAdapter("attend")
fun setAttendType(imageView: ImageView, attendType: LectureAttendType) {
    val resourceId = when (attendType) {
        LectureAttendType.PORTAL, LectureAttendType.USER -> R.drawable.ic_lecture_attend
        LectureAttendType.SIMILAR -> R.drawable.ic_lecture_attend_similar
        else -> R.drawable.ic_lecture_attend_not
    }

    imageView.setImageResource(resourceId)
}

@BindingAdapter("date")
fun setDateText(textView: TextView, date: Date) {
    textView.text = date.formatToYearMonthDay()
}

@BindingAdapter("week", "period", requireAll = true)
fun setWeekPeriodText(textView: TextView, weekType: ClassWeekType, period: Int) {
    textView.apply {
        text = context.getString(
                R.string.text_week_period,
                weekType.fullDisplayName.replace("曜日", "曜"),
                period.formatPeriod())
    }
}

@BindingAdapter("syllabus")
fun setSyllabusText(textView: TextView, syllabus: String) {
    textView.text = if (syllabus.isBlank()) "未入力" else "http://www.syllabus.kit.ac.jp/?c=detail&schedule_code=$syllabus"
}

private fun Int.formatPeriod() = if (this > 0) "${this}限" else ""