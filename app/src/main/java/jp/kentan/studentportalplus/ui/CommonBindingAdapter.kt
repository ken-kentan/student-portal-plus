package jp.kentan.studentportalplus.ui

import android.graphics.Typeface
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.vo.MyCourseType
import jp.kentan.studentportalplus.util.formatYearMonthDay
import jp.kentan.studentportalplus.view.widget.MaterialArrayAdapter
import java.util.Date

object CommonBindingAdapter {

    @JvmStatic
    @BindingAdapter("isVisible")
    fun setIsVisible(view: View, isVisible: Boolean) {
        view.isVisible = isVisible
    }

    @JvmStatic
    @BindingAdapter("background")
    fun setBackground(view: View, @ColorRes colorResId: Int) {
        view.setBackgroundResource(colorResId)
    }

    @JvmStatic
    @BindingAdapter("bold")
    fun setBold(view: TextView, isBold: Boolean) {
        view.setTypeface(null, if (isBold) Typeface.BOLD else Typeface.NORMAL)
    }

    @JvmStatic
    @BindingAdapter("date")
    fun setDate(view: TextView, date: Date?) {
        view.text = date?.formatYearMonthDay()
    }

    @JvmStatic
    @BindingAdapter("onEditorActionListener")
    fun setOnEditorActionListener(view: TextView, listener: TextView.OnEditorActionListener) {
        view.setOnEditorActionListener(listener)
    }

    @JvmStatic
    @BindingAdapter("error")
    fun setError(view: TextInputLayout, @StringRes resId: Int?) {
        if (resId == null) {
            view.isErrorEnabled = false
        } else {
            view.error = view.context.getString(resId)
        }
    }

    @JvmStatic
    @BindingAdapter("entitiesWithoutFilter")
    fun setEntitiesWithoutFilter(view: AutoCompleteTextView, entityList: List<String>) {
        val adapter = MaterialArrayAdapter(
            view.context,
            R.layout.item_dropdown_menu_popup,
            entityList
        )
        view.setAdapter(adapter)
        view.filters = emptyArray()
    }

    @JvmStatic
    @BindingAdapter("myCourseType")
    fun setMyCourseType(view: ImageView, myCourseType: MyCourseType?) {
        val resourceId = when (myCourseType) {
            MyCourseType.EDITABLE, MyCourseType.NOT_EDITABLE -> R.drawable.all_my_course_red
            MyCourseType.SIMILAR -> R.drawable.all_my_course_yellow
            else -> R.drawable.all_my_course_grey
        }

        view.setImageResource(resourceId)
    }

    @JvmStatic
    @BindingAdapter("selectedItemPosition")
    fun setSelectedItemPosition(view: Spinner, position: Int) {
        view.setSelection(position)
    }
}
