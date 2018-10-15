package jp.kentan.studentportalplus.util

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.LectureAttend
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.data.model.Notice
import java.util.*


@BindingAdapter("isVisible")
fun setIsVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}

@BindingAdapter("date")
fun setDate(view: TextView, date: Date?) {
    view.text = date?.formatYearMonthDay()
}

@BindingAdapter("noticeDate")
fun setNoticeDate(view: TextView, data: Notice?) {
    view.text = if (data != null) {
        view.context.getString(R.string.text_created_date, data.createdDate.formatYearMonthDay())
    } else {
        null
    }
}

@BindingAdapter("lectureInfoDate")
fun setLectureInfoDate(view: TextView, data: LectureInformation?) {
    if (data == null) {
        view.text = null
        return
    }

    val context = view.context

    view.text = context.getString(R.string.text_lecture_info_created_date, data.createdDate.formatYearMonthDay())

    if (data.createdDate != data.updatedDate) {
        view.append(context.getString(R.string.text_lecture_info_updated_date, data.updatedDate.formatYearMonthDay()))
    }
}

@BindingAdapter("lectureCancelDate")
fun setLectureCancelDate(view: TextView, data: LectureCancellation?) {
    view.text = if (data != null) {
        view.context.getString(R.string.text_lecture_cancel_created_date, data.createdDate.formatYearMonthDay())
    } else {
        null
    }
}

@BindingAdapter("myClassWeekPeriod")
fun setMyClassWeekPeriod(view: TextView, data: MyClass?) {
    if (data == null) {
        view.text = null
        return
    }

    val period: String = data.period.let {
        if (it > 0) "${it}限" else ""
    }

    view.text = view.context.getString(
            R.string.text_my_class_week_period,
            data.week.fullDisplayName.replace("曜日", "曜"),
            period)
}

@BindingAdapter("myClassDayPeriod")
fun setMyClassDayPeriod(view: TextView, data: MyClass?) {
    if (data == null) {
        view.text = null
        return
    }

    view.text = if (data.week.hasPeriod()) data.week.displayName + data.period else data.week.displayName
}

@BindingAdapter("lectureInfoWeekPeriod")
fun setLectureInfoWeekPeriod(view: TextView, data: LectureInformation?) {
    if (data == null) {
        view.text = null
        return
    }

    val semester: String = data.semester.let {
        if (arrayOf("前", "後", "春", "秋").contains(it)) "${it}学期" else it
    }
    val period: String = data.period.let {
        if (it != "-") "${it}限" else ""
    }

    view.text = view.context.getString(
            R.string.text_semester_week_period,
            data.grade,
            semester,
            data.week.replace("曜日", "曜"),
            period)
}

@BindingAdapter("lectureCancelWeekPeriod")
fun setLectureCancelWeekPeriod(view: TextView, data: LectureCancellation?) {
    if (data == null) {
        view.text = null
        return
    }

    val period: String = data.period.let {
        if (it != "-") "${it}限" else ""
    }

    view.text = view.context.getString(
            R.string.text_grade_week_period,
            data.grade,
            data.week.replace("曜日", "曜"),
            period)
}

@BindingAdapter("syllabus")
fun setSyllabusText(view: TextView, scheduleCode: String?) {
    view.text = if (scheduleCode.isNullOrBlank()) "未入力" else "http://www.syllabus.kit.ac.jp/?c=detail&schedule_code=$scheduleCode"
}

@BindingAdapter("bold")
fun setBold(view: TextView, isBold: Boolean) {
    view.setTypeface(null, if (isBold) Typeface.BOLD else Typeface.NORMAL)
}

@BindingAdapter("entities")
fun setAdapterEntities(view: AppCompatAutoCompleteTextView, list: List<String>?) {
    if (list != null) {
        view.setAdapter(ArrayAdapter(view.context, android.R.layout.simple_list_item_1, list))
    }
}

@BindingAdapter("attend")
fun setAttendType(view: ImageView, attendType: LectureAttend) {
    val resourceId = when (attendType) {
        LectureAttend.PORTAL, LectureAttend.USER -> R.drawable.ic_lecture_attend
        LectureAttend.SIMILAR -> R.drawable.ic_lecture_attend_similar
        else -> R.drawable.ic_lecture_attend_not
    }

    view.setImageResource(resourceId)
}

@BindingAdapter("onNavigationItemSelected")
fun setOnNavigationItemSelected(view: NavigationView, listener: NavigationView.OnNavigationItemSelectedListener?) {
    view.setNavigationItemSelectedListener(listener)
}

@BindingAdapter("backgroundColor")
fun setBackgroundColor(button: MaterialButton, color: Int) {
    button.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
}