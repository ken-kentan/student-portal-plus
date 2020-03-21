package jp.kentan.studentportalplus.ui.timetable

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class TimetableLayoutManager : RecyclerView.LayoutManager() {

    companion object {
        const val COLUMN_COUNT = 6
        private const val ROW_COUNT = 7

        private const val COURSE_COLUMN_COUNT = COLUMN_COUNT - 1
    }

    var currentFirstRow = 0
        private set

    private var currentLastRow = 0

    override fun generateDefaultLayoutParams() = RecyclerView.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)

        currentFirstRow = 0
        currentLastRow = 0

        if (itemCount == 0) {
            return
        }

        val spaceBottom = height - paddingBottom

        var offsetTop = paddingTop

        for (row in 0 until ROW_COUNT) {
            offsetTop = addRow(row, offsetTop, recycler)
            currentLastRow = row

            if (offsetTop > spaceBottom) {
                break
            }
        }
    }

    override fun canScrollVertically() = true

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (dy == 0) return 0

        val firstItem = getChildAt(0) ?: return 0
        val lastItem = getChildAt(childCount - 1) ?: return 0
        val firstTop = getDecoratedTop(firstItem)
        val lastBottom = getDecoratedBottom(lastItem)

        val scrollAmount = calculateScrollAmount(dy, firstTop, lastBottom)

        if (dy > 0) { // upper swipe
            val firstBottom = getDecoratedBottom(firstItem)

            if (lastBottom - scrollAmount < height - paddingBottom && currentLastRow < ROW_COUNT - 1) {
                addRow(++currentLastRow, lastBottom, recycler)
            }
            if (firstBottom - scrollAmount < paddingTop) {
                removeColumn(currentFirstRow, recycler)
            }
        } else {
            val lastTop = getDecoratedTop(lastItem)

            if (firstTop - scrollAmount > paddingTop) {
                addRow(--currentFirstRow, firstTop - firstItem.height, recycler, false)
            }
            if (lastTop - scrollAmount >= height - paddingBottom) {
                removeColumn(currentLastRow, recycler)
            }
        }

        offsetChildrenVertical(-scrollAmount)
        return scrollAmount
    }

    private fun removeColumn(row: Int, recycler: RecyclerView.Recycler) {
        when (row) {
            currentFirstRow -> {
                for (index in 5 downTo 0) {
                    removeAndRecycleViewAt(index, recycler)
                }

                currentFirstRow++
            }
            currentLastRow -> {
                val offset = childCount - COLUMN_COUNT
                for (index in 5 downTo 0) {
                    removeAndRecycleViewAt(offset + index, recycler)
                }
                currentLastRow--
            }
            else -> return
        }
    }

    private fun calculateScrollAmount(dy: Int, firstItemTop: Int, lastItemBottom: Int): Int {
        return if (dy > 0) { // upper swipe
            if (currentLastRow >= ROW_COUNT - 1) {
                min(dy, max(lastItemBottom - height + paddingBottom, 0))
            } else
                dy
        } else {
            if (currentFirstRow <= 0)
                max(dy, min(-(paddingTop - firstItemTop), 0))
            else
                dy
        }
    }

    private fun addRow(
        row: Int,
        offsetTop: Int,
        recycler: RecyclerView.Recycler,
        isAppend: Boolean = true
    ): Int {
        val period = recycler.getViewForPosition(row * COLUMN_COUNT)

        var index = -1
        if (isAppend) addView(period) else addView(period, ++index)

        val cache = computeSpecCache(period)

        var left = paddingLeft
        val bottom = offsetTop + cache.height

        layoutDecorated(
            period,
            left,
            offsetTop,
            left + cache.periodWidth,
            bottom
        )
        left += cache.periodWidth

        val startPosition = (row * COLUMN_COUNT) + 1
        val endPosition = startPosition + COURSE_COLUMN_COUNT

        for (position in startPosition until endPosition) {
            val child = recycler.getViewForPosition(position)
            if (isAppend) addView(child) else addView(child, ++index)

            measureChildWithCache(child, cache)

            layoutDecoratedWithMargins(
                child,
                left,
                offsetTop,
                left + cache.courseWidth,
                bottom
            )
            left += cache.courseWidth
        }

        return bottom
    }

    private fun measureChildWithCache(child: View, specCache: SpecCache) {
        val widthSpec =
            View.MeasureSpec.makeMeasureSpec(specCache.courseWidth, View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(specCache.height, View.MeasureSpec.EXACTLY)

        child.measure(widthSpec, heightSpec)
    }

    private fun computeSpecCache(period: View): SpecCache {
        measureChild(period, 0, 0)

        return SpecCache(
            height = getDecoratedMeasuredHeight(period),
            periodWidth = getDecoratedMeasuredWidth(period)
        )
    }

    private inner class SpecCache(
        val height: Int,
        val periodWidth: Int
    ) {
        val courseWidth = ((width - periodWidth).toDouble() / COURSE_COLUMN_COUNT).roundToInt()
    }
}
