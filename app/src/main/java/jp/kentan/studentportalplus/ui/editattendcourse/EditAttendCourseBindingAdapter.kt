package jp.kentan.studentportalplus.ui.editattendcourse

import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter
import jp.kentan.studentportalplus.R
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
}