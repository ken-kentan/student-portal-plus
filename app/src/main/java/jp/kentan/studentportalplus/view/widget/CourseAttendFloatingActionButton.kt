package jp.kentan.studentportalplus.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnticipateOvershootInterpolator
import jp.kentan.studentportalplus.data.entity.AttendCourse

class CourseAttendFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.floatingActionButtonStyle
) : CheckableFloatingActionButton(context, attrs, defStyleAttr) {

    private companion object {
        const val ROTATION_FROM = 0f
        const val ROTATION_TO = 135f
        const val DURATION = 800L
    }

    private val interpolator = AnticipateOvershootInterpolator()
    private var isInitialized = false

    fun setAttendType(attendType: AttendCourse.Type?) {
        attendType ?: return

        if (attendType == AttendCourse.Type.PORTAL) {
            hide()
        } else {
            show()
            isChecked = attendType == AttendCourse.Type.USER
        }
    }

    override fun onCheckChanged(isChecked: Boolean) {
        if (!isInitialized) {
            rotation = if (isChecked) ROTATION_TO else ROTATION_FROM
            isInitialized = true
            return
        }

        animate()
            .rotation(if (isChecked) ROTATION_TO else ROTATION_FROM)
            .setDuration(DURATION)
            .setInterpolator(interpolator)
            .start()
    }
}
