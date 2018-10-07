package jp.kentan.studentportalplus.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class DividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val bounds = Rect()
    private val divider: Drawable

    init {
        val attrs = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))

        divider = attrs.getDrawable(0) ?: throw IllegalArgumentException("@android:attr/listDivider was not set.")
        attrs.recycle()
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        canvas.save()

        val left: Int
        val right: Int

        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        val childCount = parent.childCount
        for (i in 0..childCount - 2) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + Math.round(child.translationY)
            val top = bottom - divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(canvas)
        }

        canvas.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        // Last position
        if (parent.getChildAdapterPosition(view) == state.itemCount -1) {
            outRect.set(0, 0, 0, 0)
            return
        }

        outRect.set(0, 0, 0, divider.intrinsicHeight)
    }
}