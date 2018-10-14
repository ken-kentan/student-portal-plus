package jp.kentan.studentportalplus.util

import android.animation.AnimatorInflater
import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import jp.kentan.studentportalplus.R

fun View.animateFadeInDelay(context: Context, delay: Long = 200) {
    if (isVisible) {
        return
    }

    AnimatorInflater.loadAnimator(context, R.animator.fade_in).apply {
        setTarget(this@animateFadeInDelay)
        startDelay = delay
        start()
    }

    isVisible = true
}