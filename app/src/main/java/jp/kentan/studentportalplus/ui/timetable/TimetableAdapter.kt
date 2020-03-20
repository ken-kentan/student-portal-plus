package jp.kentan.studentportalplus.ui.timetable

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period
import jp.kentan.studentportalplus.databinding.ItemGridAttendCourseBinding
import jp.kentan.studentportalplus.databinding.ItemGridBlankBinding
import jp.kentan.studentportalplus.databinding.ItemGridPeriodBinding
import jp.kentan.studentportalplus.databinding.ItemListAttendCourseBinding
import jp.kentan.studentportalplus.util.executeAfter

class TimetableAdapter(
    private val layout: Layout,
    private val onAttendCourseClick: (Long) -> Unit,
    private val onBlankClick: ((Period, DayOfWeek) -> Unit)? = null
) : RecyclerView.Adapter<TimetableAdapter.ViewHolder>() {

    enum class Layout { GRID, LIST }

    private val differ = AsyncListDiffer<Any>(this, DiffCallback)

    private val attendCourseItemViewType = when (layout) {
        Layout.GRID -> R.layout.item_grid_attend_course
        Layout.LIST -> R.layout.item_list_attend_course
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int) = when (differ.currentList[position]) {
        is Period -> R.layout.item_grid_period
        is Blank -> R.layout.item_grid_blank
        is AttendCourse -> attendCourseItemViewType
        else -> throw IllegalStateException("Unknown view type at position $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.item_grid_period -> ViewHolder.PeriodViewHolder(
                ItemGridPeriodBinding.inflate(inflater, parent, false)
            )
            R.layout.item_grid_blank -> ViewHolder.BlankViewHolder(
                ItemGridBlankBinding.inflate(inflater, parent, false)
            )
            R.layout.item_grid_attend_course -> ViewHolder.GridAttendCourseViewHolder(
                ItemGridAttendCourseBinding.inflate(inflater, parent, false)
            )
            R.layout.item_list_attend_course -> ViewHolder.ListAttendCourseViewHolder(
                ItemListAttendCourseBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.GridAttendCourseViewHolder -> holder.binding.executeAfter {
                val attendCourse = differ.currentList[position] as AttendCourse
                data = attendCourse
                root.setOnClickListener {
                    onAttendCourseClick(attendCourse.id)
                }
            }
            is ViewHolder.ListAttendCourseViewHolder -> holder.binding.executeAfter {
                val attendCourse = differ.currentList[position] as AttendCourse
                data = attendCourse
                root.setOnClickListener {
                    onAttendCourseClick(attendCourse.id)
                }
            }
            is ViewHolder.BlankViewHolder -> holder.binding.executeAfter {
                val blank = differ.currentList[position] as Blank
                root.setOnClickListener {
                    onBlankClick?.invoke(blank.period, blank.dayOfWeek)
                }
            }
            is ViewHolder.PeriodViewHolder -> holder.binding.executeAfter {
                period = differ.currentList[position] as Period
            }
        }
    }

    fun submitList(list: List<AttendCourse>) {
        when (layout) {
            Layout.GRID -> differ.submitList(list.asTimetable())
            Layout.LIST -> differ.submitList(list)
        }
    }

    private fun List<AttendCourse>.asTimetable(): List<Any> {
        val list = mutableListOf<Any>()

        for (period in Period.values()) {
            list.add(period)

            for (dayOfWeek in DayOfWeek.WEEKDAY) {
                list.add(
                    find { it.period == period.value && it.dayOfWeek == dayOfWeek } ?: Blank(
                        period,
                        dayOfWeek
                    )
                )
            }
        }

        return list
    }

    data class Blank(
        val period: Period,
        val dayOfWeek: DayOfWeek
    )

    object DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Blank && newItem is Blank -> newItem == oldItem
                oldItem is Period && newItem is Period -> newItem == oldItem
                oldItem is AttendCourse && newItem is AttendCourse -> oldItem.id == newItem.id
                else -> false
            }
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is AttendCourse && newItem is AttendCourse -> oldItem == newItem
                else -> true
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class GridAttendCourseViewHolder(
            val binding: ItemGridAttendCourseBinding
        ) : ViewHolder(binding.root)

        class ListAttendCourseViewHolder(
            val binding: ItemListAttendCourseBinding
        ) : ViewHolder(binding.root)

        class PeriodViewHolder(
            val binding: ItemGridPeriodBinding
        ) : ViewHolder(binding.root)

        class BlankViewHolder(
            val binding: ItemGridBlankBinding
        ) : ViewHolder(binding.root)
    }
}
