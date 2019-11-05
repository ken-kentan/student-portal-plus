package jp.kentan.studentportalplus.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.animation.OvershootInterpolator
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.Notice

class NoticeFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.floatingActionButtonStyle
) : CheckableFloatingActionButton(context, attrs, defStyleAttr) {

    private companion object {
        const val ROTATION_FROM = 0f
        const val ROTATION_TO = 144f
        const val DURATION = 800L
    }

    private val interpolator = OvershootInterpolator()
    private var isInitialized = false

    fun setNotice(notice: Notice?) {
        if (notice == null) {
            return
        }

        isChecked = notice.isFavorite
    }

    override fun onCheckChanged(isChecked: Boolean) {
        setImageResource(if (isChecked) R.drawable.ic_favorite_grey_24dp else R.drawable.ic_favorite_border_white_24dp)

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
