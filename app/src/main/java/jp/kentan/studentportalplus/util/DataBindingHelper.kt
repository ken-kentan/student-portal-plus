package jp.kentan.studentportalplus.util

import android.databinding.BindingAdapter
import android.graphics.Typeface
import android.widget.ImageView
import android.widget.TextView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.LectureAttendType
import java.util.*

@BindingAdapter("isRead")
fun setIsRead(textView: TextView, isRead: Boolean) {
    textView.typeface = if (isRead) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
}

@BindingAdapter("attendType")
fun setAttendType(imageView: ImageView, attendType: LectureAttendType) {
    val resourceId = when (attendType) {
        LectureAttendType.PORTAL, LectureAttendType.USER -> {
            R.drawable.ic_lecture_attend
        }
        LectureAttendType.SIMILAR -> {
            R.drawable.ic_lecture_attend_similar
        }
        else -> {
            R.drawable.ic_lecture_attend_not
        }
    }

    imageView.setImageResource(resourceId)
}

@BindingAdapter("dateText")
fun setDateText(textView: TextView, date: Date) {
    textView.text = date.formatToYearMonthDay()
}