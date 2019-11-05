package jp.kentan.studentportalplus.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.databinding.ItemSmallAttendCourseBinding
import jp.kentan.studentportalplus.databinding.ItemSmallHeaderBinding
import jp.kentan.studentportalplus.util.executeAfter

class TimetableAdapter(
    private val parentCardView: CardView,
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<TimetableAdapter.ViewHolder>() {

    private var currentList: List<Any> = emptyList()

    override fun getItemCount() = currentList.size

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is Header -> R.layout.item_small_header
        is Item -> R.layout.item_small_attend_course
        else -> throw IllegalStateException("Unknown view type at position $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.item_small_header -> ViewHolder.HeaderViewHolder(
                ItemSmallHeaderBinding.inflate(inflater, parent, false)
            )
            R.layout.item_small_attend_course -> ViewHolder.AttendCourseViewHolder(
                ItemSmallAttendCourseBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.HeaderViewHolder -> holder.binding.executeAfter {
                textView.apply {
                    val header = currentList[position] as Header

                    text = context.getString(
                        R.string.text_today_timetable,
                        context.getString(header.dayOfWeek.resId)
                    )
                }
            }
            is ViewHolder.AttendCourseViewHolder -> holder.binding.executeAfter {
                val item = currentList[position] as Item

                data = item.attendCourse
                layout.setOnClickListener { onItemClick(item.attendCourse.id) }
                dividerView.isGone = item.isLastPosition
            }
        }
    }

    fun submitList(list: List<AttendCourse>) {
        val newList = mutableListOf<Any>()
        val oldList = currentList

        if (list.isNotEmpty()) {
            // header
            newList.add(Header(list.first().dayOfWeek))

            // data
            newList.addAll(list.mapIndexed { index, attendCourse ->
                Item(attendCourse, index >= list.lastIndex)
            })
        }

        val result = DiffUtil.calculateDiff(
            Callback(
                oldList,
                newList
            )
        )

        currentList = newList

        parentCardView.isVisible = newList.isNotEmpty()

        result.dispatchUpdatesTo(this)
    }


    private data class Header(
        val dayOfWeek: DayOfWeek
    )

    private data class Item(
        val attendCourse: AttendCourse,
        val isLastPosition: Boolean
    )


    private class Callback(
        private val oldList: List<Any>,
        private val newList: List<Any>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return when {
                oldItem is Header && newItem is Header -> true
                oldItem is Item && newItem is Item -> oldItem.attendCourse.id == newItem.attendCourse.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return when {
                oldItem is Header && newItem is Header -> oldItem == newItem
                oldItem is Item && newItem is Item -> oldItem == newItem
                else -> true
            }
        }
    }


    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class HeaderViewHolder(
            val binding: ItemSmallHeaderBinding
        ) : ViewHolder(binding.root)

        class AttendCourseViewHolder(
            val binding: ItemSmallAttendCourseBinding
        ) : ViewHolder(binding.root)
    }
}