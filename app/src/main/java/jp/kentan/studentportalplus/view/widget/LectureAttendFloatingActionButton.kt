package jp.kentan.studentportalplus.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnticipateOvershootInterpolator
import jp.kentan.studentportalplus.data.component.LectureAttend

class LectureAttendFloatingActionButton @JvmOverloads constructor(
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

    fun setAttend(attend: LectureAttend?) {
        attend ?: return

        if (attend == LectureAttend.PORTAL) {
            hide()
        } else {
            show()
            isChecked = attend == LectureAttend.USER
        }
    }

    override fun onCheckedChange() {
        val isAttend = isChecked

        if (!isInitialized) {
            rotation = if (isAttend) ROTATION_TO else ROTATION_FROM
            isInitialized = true
            return
        }

        animate()
                .rotation(if (isAttend) ROTATION_TO else ROTATION_FROM)
                .setDuration(DURATION)
                .setInterpolator(interpolator)
                .start()
    }
}