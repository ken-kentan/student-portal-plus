package jp.kentan.studentportalplus.util

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation


class AnimationHelper {

    companion object {
        private val FADE_IN  = AlphaAnimation(0f, 1f)
        private val FADE_OUT = AlphaAnimation(1f, 0f)

        fun fadeIn(view: View): Animation {
            FADE_IN.duration = 180
            FADE_IN.startOffset = 360

            view.visibility = View.VISIBLE

            return FADE_IN
        }

        fun fadeOut(view: View): Animation {
            FADE_OUT.duration = 180
            FADE_OUT.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    view.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })

            return FADE_OUT
        }
    }
}