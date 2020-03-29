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
import jp.kentan.studentportalplus.data.entity.MyCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.databinding.ItemSmallHeaderBinding
import jp.kentan.studentportalplus.databinding.ItemSmallMyCourseBinding
import jp.kentan.studentportalplus.util.executeAfter

class TimetableAdapter(
    private val parentCardView: CardView,
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<TimetableAdapter.ViewHolder>() {

    private var currentList: List<Any> = emptyList()

    override fun getItemCount() = currentList.size

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is Header -> R.layout.item_small_header
        is Item -> R.layout.item_small_my_course
        else -> throw IllegalStateException("Unknown view type at position $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.item_small_header -> ViewHolder.HeaderViewHolder(
                ItemSmallHeaderBinding.inflate(inflater, parent, false)
            )
            R.layout.item_small_my_course -> ViewHolder.MyCourseViewHolder(
                ItemSmallMyCourseBinding.inflate(inflater, parent, false)
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
                        R.string.dashboard_today_my_course,
                        context.getString(header.dayOfWeek.resId)
                    )
                }
            }
            is ViewHolder.MyCourseViewHolder -> holder.binding.executeAfter {
                val item = currentList[position] as Item

                data = item.myCourse
                layout.setOnClickListener { onItemClick(item.myCourse.id) }
                dividerView.isGone = item.isLastPosition
            }
        }
    }

    fun submitList(list: List<MyCourse>) {
        val newList = mutableListOf<Any>()
        val oldList = currentList

        if (list.isNotEmpty()) {
            // header
            newList.add(Header(list.first().dayOfWeek))

            // data
            newList.addAll(list.mapIndexed { index, myCourse ->
                Item(myCourse, index >= list.lastIndex)
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
        val myCourse: MyCourse,
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
                oldItem is Item && newItem is Item -> oldItem.myCourse.id == newItem.myCourse.id
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

        class MyCourseViewHolder(
            val binding: ItemSmallMyCourseBinding
        ) : ViewHolder(binding.root)
    }
}
