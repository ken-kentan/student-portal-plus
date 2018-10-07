package jp.kentan.studentportalplus.view.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import com.google.android.material.floatingactionbutton.FloatingActionButton

abstract class CheckableFloatingActionButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = com.google.android.material.R.attr.floatingActionButtonStyle
) : FloatingActionButton(context, attrs, defStyleAttr), Checkable {

    private var isChecked = false

    override fun isChecked() = isChecked

    override fun toggle() {
        isChecked = !isChecked
        onCheckedChange()
    }

    override fun setChecked(checked: Boolean) {
        isChecked = checked
        onCheckedChange()
    }

    abstract fun onCheckedChange()
}