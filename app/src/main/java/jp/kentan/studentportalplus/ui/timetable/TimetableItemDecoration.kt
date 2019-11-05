package jp.kentan.studentportalplus.ui.timetable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import java.util.*
import kotlin.math.roundToInt

class TimetableItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    companion object {
        private val START_TIME_MINUTES = intArrayOf(
            8 * 60 + 50,
            10 * 60 + 30,
            12 * 60 + 50,
            14 * 60 + 30,
            16 * 60 + 10,
            17 * 60 + 50,
            19 * 60 + 30
        )

        private const val COURSE_MINUTES = 90
    }

    private val linePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.primary)
    }
    private val maskPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.timetable_grid_mask)
    }
    private val backgroundPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.timetable_grid_background)
    }

    private val lineHeight = 10 // TODO
    private val bounds = Rect()

    private val calender = Calendar.getInstance()

    private var currentDayOfWeek = calender.get(Calendar.DAY_OF_WEEK)
    private var currentMinutes =
        calender.get(Calendar.MINUTE) + calender.get(Calendar.HOUR_OF_DAY) * 60

    fun syncCalenderByCurrentTime() {
        calender.timeInMillis = System.currentTimeMillis()

        currentDayOfWeek = calender.get(Calendar.DAY_OF_WEEK)
        currentMinutes = calender.get(Calendar.MINUTE) + calender.get(Calendar.HOUR_OF_DAY) * 60
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.childCount <= 0) {
            return
        }

        val dayOfWeek = currentDayOfWeek
        val minutes = currentMinutes

        val rowOffset = parent.layoutManager.getCurrentFirstRow()

        c.save()

        for (index in 1 until parent.childCount) {
            val weekday = (index % TimetableLayoutManager.COLUMN_COUNT) + 1

            if (weekday == Calendar.SUNDAY) { // Period sections
                continue
            }

            val child = parent.getChildAt(index)
            parent.getDecoratedBoundsWithMargins(child, bounds)

            if (weekday < dayOfWeek) {
                c.drawRect(bounds, maskPaint)
            } else if (weekday == dayOfWeek) {
                val period = (index / TimetableLayoutManager.COLUMN_COUNT) + rowOffset // range 0..6

                val diffMinutes = minutes - START_TIME_MINUTES[period]

                if (diffMinutes < 0) { // future
                    c.drawRect(bounds, backgroundPaint)
                    continue
                }

                if (diffMinutes > COURSE_MINUTES) { // past
                    c.drawRect(bounds, maskPaint)
                    continue
                }

                val ratio = diffMinutes.toDouble() / COURSE_MINUTES

                val maskTop = bounds.top
                val height = bounds.bottom - maskTop
                val maskBottom = maskTop + (height * ratio).roundToInt()

                val maskRect = Rect(
                    bounds.left,
                    maskTop,
                    bounds.right,
                    maskBottom
                )
                c.drawRect(maskRect, maskPaint)

                val backgroundRect = Rect(
                    maskRect.left,
                    maskBottom,
                    maskRect.right,
                    bounds.bottom
                )
                c.drawRect(backgroundRect, backgroundPaint)
            } else {
                c.drawRect(bounds, backgroundPaint)
            }
        }

        c.restore()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.childCount <= 0) {
            return
        }

        val dayOfWeek = currentDayOfWeek
        val minutes = currentMinutes

        val rowOffset = parent.layoutManager.getCurrentFirstRow()

        c.save()

        for (index in 1 until parent.childCount) {
            val weekday = (index % TimetableLayoutManager.COLUMN_COUNT) + 1
            if (weekday != dayOfWeek || weekday == Calendar.SUNDAY) {
                continue
            }

            val period = (index / TimetableLayoutManager.COLUMN_COUNT) + rowOffset // range 0..6

            val diffMinutes = minutes - START_TIME_MINUTES[period]
            if (diffMinutes < 0 || diffMinutes > COURSE_MINUTES) {
                continue
            }

            val child = parent.getChildAt(index)
            parent.getDecoratedBoundsWithMargins(child, bounds)

            val ratio = diffMinutes.toDouble() / COURSE_MINUTES

            val height = bounds.bottom - bounds.top
            val top = bounds.top + ((height - lineHeight).toDouble() * ratio).roundToInt()

            val rect = Rect(
                bounds.left,
                top,
                bounds.right,
                top + lineHeight
            )
            c.drawRect(rect, linePaint)
            break
        }

        c.restore()
    }

    private fun RecyclerView.LayoutManager?.getCurrentFirstRow() =
        (this as TimetableLayoutManager).currentFirstRow
}