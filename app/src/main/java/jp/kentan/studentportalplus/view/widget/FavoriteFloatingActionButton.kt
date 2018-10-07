package jp.kentan.studentportalplus.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.animation.OvershootInterpolator
import jp.kentan.studentportalplus.R

class FavoriteFloatingActionButton @JvmOverloads constructor(
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

    override fun onCheckedChange() {
        val isFavorite = isChecked

        setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)

        if (!isInitialized) {
            rotation = if (isFavorite) ROTATION_TO else ROTATION_FROM
            isInitialized = true
            return
        }

        animate()
                .rotation(if (isFavorite) ROTATION_TO else ROTATION_FROM)
                .setDuration(DURATION)
                .setInterpolator(interpolator)
                .start()
    }

}