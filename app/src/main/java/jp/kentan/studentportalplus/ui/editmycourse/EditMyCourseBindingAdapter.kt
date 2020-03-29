package jp.kentan.studentportalplus.ui.editmycourse

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import jp.kentan.studentportalplus.data.vo.CourseColor

object EditMyCourseBindingAdapter {

    @JvmStatic
    @BindingAdapter("courseColor")
    fun setBackgroundColor(view: MaterialButton, courseColor: CourseColor?) {
        val color = ContextCompat.getColor(view.context, courseColor?.resId ?: return)
        view.setBackgroundColor(color)
    }

    @JvmStatic
    @BindingAdapter("entities")
    fun setEntities(view: AutoCompleteTextView, entityList: List<String>?) {
        val adapter = ArrayAdapter(
            view.context,
            android.R.layout.simple_list_item_1,
            entityList ?: emptyList()
        )
        view.setAdapter(adapter)
    }
}
