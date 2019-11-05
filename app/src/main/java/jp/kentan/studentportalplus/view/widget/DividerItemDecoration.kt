package jp.kentan.studentportalplus.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.getDrawableOrThrow
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class DividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val divider: Drawable
    private val bounds = Rect()

    init {
        context.obtainStyledAttributes(
            intArrayOf(android.R.attr.listDivider)
        ).run {
            divider = getDrawableOrThrow(0)
            recycle()
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        canvas.save()
        val left: Int
        val right: Int
        //noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(
                left, parent.paddingTop, right,
                parent.height - parent.paddingBottom
            )
        } else {
            left = 0
            right = parent.width
        }

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + child.translationY.roundToInt()
            val top = bottom - divider.intrinsicHeight
            divider.alpha = (child.alpha * 255f).roundToInt()
            divider.setBounds(left, top, right, bottom)
            divider.draw(canvas)
        }
        canvas.restore()
    }


    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(0, 0, 0, divider.intrinsicHeight)
    }
}