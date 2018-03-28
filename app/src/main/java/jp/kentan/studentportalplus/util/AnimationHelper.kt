package jp.kentan.studentportalplus.util

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.view.View
import jp.kentan.studentportalplus.R


fun View.animateFadeIn(context: Context) {
    if (visibility == View.VISIBLE) {
        return
    }

    val fadeIn = AnimatorInflater.loadAnimator(context, R.animator.fade_in)

    fadeIn.setTarget(this)
    fadeIn.start()

    visibility = View.VISIBLE
}

fun View.animateFadeInDelay(context: Context, delay: Long = 200) {
    if (visibility == View.VISIBLE) {
        return
    }

    val fadeIn = AnimatorInflater.loadAnimator(context, R.animator.fade_in)

    fadeIn.setTarget(this)
    fadeIn.startDelay = delay
    fadeIn.start()

    visibility = View.VISIBLE
}

fun View.animateFadeOut(context: Context) {
    if (visibility != View.VISIBLE) {
        return
    }

    val fadeOut = AnimatorInflater.loadAnimator(context, R.animator.fade_out)

    fadeOut.addListener(object : Animator.AnimatorListener{
        override fun onAnimationRepeat(animation: Animator?) {}

        override fun onAnimationEnd(animation: Animator?) {
            visibility = View.GONE
        }

        override fun onAnimationCancel(animation: Animator?) {}

        override fun onAnimationStart(animation: Animator?) {}
    })

    fadeOut.setTarget(this)
    fadeOut.start()
}