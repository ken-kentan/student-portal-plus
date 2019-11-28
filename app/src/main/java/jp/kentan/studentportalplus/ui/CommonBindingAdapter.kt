package jp.kentan.studentportalplus.ui

import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.util.formatYearMonthDay
import java.util.*

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
    @BindingAdapter("attendType")
    fun setAttendType(view: ImageView, attendType: AttendCourse.Type?) {
        val resourceId = when (attendType) {
            AttendCourse.Type.PORTAL, AttendCourse.Type.USER -> R.drawable.ic_attend_red_24dp
            AttendCourse.Type.SIMILAR -> R.drawable.ic_attend_yellow_24dp
            else -> R.drawable.ic_attend_grey_24dp
        }

        view.setImageResource(resourceId)
    }

    @JvmStatic
    @BindingAdapter("selectedItemPosition")
    fun setSelectedItemPosition(view: Spinner, position: Int) {
        view.setSelection(position)
    }
}
