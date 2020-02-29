package jp.kentan.studentportalplus.ui.editattendcourse

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import jp.kentan.studentportalplus.data.vo.CourseColor

object EditAttendCourseBindingAdapter {

    @JvmStatic
    @BindingAdapter("courseColor")
    fun setBackgroundColor(view: MaterialButton, courseColor: CourseColor?) {
        val color = ContextCompat.getColor(view.context, courseColor?.resId ?: return)
        view.setBackgroundColor(color)
    }
}
