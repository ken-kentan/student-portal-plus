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

    val fadeIn = AnimatorInflater.loadAnimator(context, R.animator.fade_in_delay)

    visibility = View.VISIBLE

    fadeIn.setTarget(this)
    fadeIn.start()
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