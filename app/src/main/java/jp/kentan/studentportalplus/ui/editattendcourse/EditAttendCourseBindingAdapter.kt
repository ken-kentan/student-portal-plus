package jp.kentan.studentportalplus.ui.editattendcourse

import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.vo.CourseColor
import jp.kentan.studentportalplus.view.widget.MaterialArrayAdapter

object EditAttendCourseBindingAdapter {
    @JvmStatic
    @BindingAdapter("entities")
    fun setEntities(view: AutoCompleteTextView, entityList: List<String>) {
        val adapter = MaterialArrayAdapter(
            view.context,
            R.layout.item_dropdown_menu_popup,
            entityList
        )
        view.setAdapter(adapter)
        view.filters = emptyArray()
    }

    @JvmStatic
    @BindingAdapter("courseColor")
    fun setBackgroundColor(view: MaterialButton, courseColor: CourseColor?) {
        val color = ContextCompat.getColor(view.context, courseColor?.resId ?: return)
        view.setBackgroundColor(color)
    }
}
