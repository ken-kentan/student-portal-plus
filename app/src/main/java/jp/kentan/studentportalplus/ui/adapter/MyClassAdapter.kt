package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.model.MyClass
import kotlinx.android.synthetic.main.grid_my_class.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*


class MyClassAdapter(
        private val context: Context,
        private var viewType: Int,
        private val onClick: (data: MyClass) -> Unit,
        private val onAddClick: (period: Int, week: ClassWeekType) -> Unit
) : ListAdapter<MyClass, MyClassAdapter.ViewHolder>(MyClass.DIFF_CALLBACK) {

    companion object {
        private const val TYPE_EMPTY = -1
        const val TYPE_GRID  = 0
        const val TYPE_LIST  = 1

        private val PERIOD_MINUTES = intArrayOf(8 * 60 + 50, 10 * 60 + 30, 12 * 60 + 50, 14 * 60 + 30, 16 * 60 + 10, 17 * 60 + 50, 19 * 60 + 30)
    }

    init {
        setHasStableIds(true)

        if (viewType != TYPE_GRID && viewType != TYPE_LIST) {
            throw IllegalArgumentException("Invalid ViewType: $viewType")
        }
    }

    fun setViewType(viewType: Int) {
        if (viewType != TYPE_GRID && viewType != TYPE_LIST) {
            throw IllegalArgumentException("Invalid ViewType: $viewType")
        }
        this.viewType = viewType
        submitList(null)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun getItemViewType(position: Int) = if (getItemId(position) < 0) TYPE_EMPTY else viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        val layoutId = when (viewType) {
            TYPE_EMPTY -> R.layout.grid_my_class_empty
            TYPE_GRID  -> R.layout.grid_my_class
            TYPE_LIST  -> R.layout.list_my_class
            else       -> R.layout.list_my_class
        }

        val view = layoutInflater.inflate(layoutId, parent, false)

        return ViewHolder(view, viewType, onClick, onAddClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))

        if (viewType == TYPE_GRID) {
            holder.setPercent(getGuidelinePercent(position))
        }
    }

    private fun getGuidelinePercent(position: Int): Float {
        val week = position % 5 + 2
        val period = position / 5

        val now = Calendar.getInstance()
        val todayWeek = now.get(Calendar.DAY_OF_WEEK)

        if (week < todayWeek || todayWeek == Calendar.SUNDAY) {
            return 1f
        } else if (week == todayWeek) {
            val minutes = now.get(Calendar.MINUTE) + now.get(Calendar.HOUR_OF_DAY) * 60

            val diff = (minutes - PERIOD_MINUTES[period])

            if (diff > 0) {
                return Math.min(diff / 90f, 1f)
            }
        }

        return 0f
    }

    class ViewHolder(
            private val view: View,
            private val viewType: Int,
            private val onClick: (data: MyClass) -> Unit,
            private val onAddClick: (period: Int, week: ClassWeekType) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        fun bindTo(data: MyClass) {
            if (viewType == TYPE_EMPTY) {
                view.find<View>(R.id.add_button).setOnClickListener { onAddClick(data.period, data.week) }
                return
            }
            when (viewType) {
               TYPE_LIST -> {
                   view.find<ImageView>(R.id.user_icon).setImageResource(
                           if (data.isUser) R.drawable.ic_lock_off else R.drawable.ic_lock_on
                   )
                   view.find<View>(R.id.color_header).backgroundColor = data.color
                   view.find<TextView>(R.id.day_and_period).text = formatDayAndPeriod(data)
               }
                TYPE_GRID -> {
                    view.location.text     = data.location
                    view.layout.backgroundColor = data.color
                }
            }

            view.subject.text    = data.subject
            view.instructor.text = data.instructor

            view.layout.onClick { onClick(data) }
        }

        fun setPercent(ratio: Float) {
            if (ratio > 0f) {
                view.guideline.setGuidelinePercent(ratio)
                view.mask_group.visibility = View.VISIBLE
            } else {
                view.mask_group.visibility = View.GONE
            }
        }

        private companion object {
            fun formatDayAndPeriod(data: MyClass): String {
                return if (data.week.hasPeriod()) {
                    data.week.displayName + data.period
                } else {
                    data.week.displayName
                }
            }
        }
    }
}