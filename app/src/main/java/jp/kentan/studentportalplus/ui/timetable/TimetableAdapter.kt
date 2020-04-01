package jp.kentan.studentportalplus.ui.timetable

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.MyCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period
import jp.kentan.studentportalplus.databinding.ItemGridBlankBinding
import jp.kentan.studentportalplus.databinding.ItemGridMyCourseBinding
import jp.kentan.studentportalplus.databinding.ItemGridPeriodBinding
import jp.kentan.studentportalplus.databinding.ItemListMyCourseBinding
import jp.kentan.studentportalplus.util.executeAfter

class TimetableAdapter(
    private val layout: Layout,
    private val onMyCourseClick: (Long) -> Unit,
    private val onBlankClick: ((Period, DayOfWeek) -> Unit)? = null
) : RecyclerView.Adapter<TimetableAdapter.ViewHolder>() {

    enum class Layout { GRID, LIST }

    private val differ = AsyncListDiffer(this, DiffCallback)

    private val myCourseItemViewType = when (layout) {
        Layout.GRID -> R.layout.item_grid_my_course
        Layout.LIST -> R.layout.item_list_my_course
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int) = when (differ.currentList[position]) {
        is Period -> R.layout.item_grid_period
        is Blank -> R.layout.item_grid_blank
        is MyCourse -> myCourseItemViewType
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
            R.layout.item_grid_my_course -> ViewHolder.GridMyCourseViewHolder(
                ItemGridMyCourseBinding.inflate(inflater, parent, false)
            )
            R.layout.item_list_my_course -> ViewHolder.ListMyCourseViewHolder(
                ItemListMyCourseBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.GridMyCourseViewHolder -> holder.binding.executeAfter {
                val myCourse = differ.currentList[position] as MyCourse
                data = myCourse
                root.setOnClickListener {
                    onMyCourseClick(myCourse.id)
                }
            }
            is ViewHolder.ListMyCourseViewHolder -> holder.binding.executeAfter {
                val myCourse = differ.currentList[position] as MyCourse
                data = myCourse
                root.setOnClickListener {
                    onMyCourseClick(myCourse.id)
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

    fun submitList(list: List<MyCourse>) {
        when (layout) {
            Layout.GRID -> differ.submitList(list.asTimetable())
            Layout.LIST -> differ.submitList(list)
        }
    }

    private fun List<MyCourse>.asTimetable(): List<Any> {
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
                oldItem is MyCourse && newItem is MyCourse -> oldItem.id == newItem.id
                else -> false
            }
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is MyCourse && newItem is MyCourse -> oldItem == newItem
                else -> true
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class GridMyCourseViewHolder(
            val binding: ItemGridMyCourseBinding
        ) : ViewHolder(binding.root)

        class ListMyCourseViewHolder(
            val binding: ItemListMyCourseBinding
        ) : ViewHolder(binding.root)

        class PeriodViewHolder(
            val binding: ItemGridPeriodBinding
        ) : ViewHolder(binding.root)

        class BlankViewHolder(
            val binding: ItemGridBlankBinding
        ) : ViewHolder(binding.root)
    }
}
