package jp.kentan.studentportalplus.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.databinding.ItemSmallHeaderBinding
import jp.kentan.studentportalplus.databinding.ItemSmallLectureInformationBinding
import jp.kentan.studentportalplus.databinding.ItemSmallShowAllBinding
import jp.kentan.studentportalplus.databinding.ItemTextBinding
import jp.kentan.studentportalplus.util.executeAfter

class LectureInformationAdapter(
    private val onItemClick: (Long) -> Unit,
    private val onShowAllClick: (() -> Unit)
) : RecyclerView.Adapter<LectureInformationAdapter.ViewHolder>() {

    private companion object {
        const val MAX_ITEM_COUNT = 3
    }

    private var currentList: List<Any> = listOf(Header())

    override fun getItemCount() = currentList.size

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is Header -> R.layout.item_small_header
        is Item -> R.layout.item_small_lecture_information
        EmptyItem -> R.layout.item_text
        ShowAll -> R.layout.item_small_show_all
        else -> throw IllegalStateException("Unknown view type at position $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.item_small_header -> ViewHolder.HeaderViewHolder(
                ItemSmallHeaderBinding.inflate(inflater, parent, false)
            )
            R.layout.item_small_lecture_information -> ViewHolder.LectureInformationViewHolder(
                ItemSmallLectureInformationBinding.inflate(inflater, parent, false)
            )
            R.layout.item_text -> ViewHolder.TextViewHolder(
                ItemTextBinding.inflate(inflater, parent, false)
            )
            R.layout.item_small_show_all -> ViewHolder.ShowAllViewHolder(
                ItemSmallShowAllBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.HeaderViewHolder -> holder.binding.executeAfter {
                val header = currentList[position] as Header

                textView.apply {
                    setText(R.string.all_lecture_information)
                    if (header.hasMoreItem) {
                        val suffix =
                            context.getString(
                                R.string.dashboard_more_items_suffix,
                                header.moreItemCount
                            )
                        append(suffix)
                    }
                }
            }
            is ViewHolder.LectureInformationViewHolder -> holder.binding.executeAfter {
                val item = (currentList[position] as Item)

                data = item.lectureInfo
                layout.setOnClickListener { onItemClick(item.lectureInfo.id) }
                dividerView.isVisible = item.isVisibleDivider
            }
            is ViewHolder.TextViewHolder -> holder.binding.executeAfter {
                textView.setText(R.string.dashboard_empty_lecture_information)
            }
            is ViewHolder.ShowAllViewHolder -> holder.binding.executeAfter {
                textView.apply {
                    setText(R.string.dashboard_open_lecture_informations)
                    setOnClickListener { onShowAllClick.invoke() }
                }
            }
        }
    }

    fun submitList(list: List<LectureInformation>) {
        val newList = mutableListOf<Any>()
        val oldList = currentList

        val header = Header(
            moreItemCount = list.size - MAX_ITEM_COUNT
        )

        // header
        newList.add(header)

        // data
        if (list.isNotEmpty()) {
            newList.addAll(list.take(MAX_ITEM_COUNT).mapIndexed { index, lectureInfo ->
                Item(
                    lectureInfo = lectureInfo,
                    isVisibleDivider = header.hasMoreItem || index + 1 < list.size
                )
            })
        } else {
            newList.add(EmptyItem)
        }

        // show all
        if (header.hasMoreItem) {
            newList.add(ShowAll)
        }

        val result = DiffUtil.calculateDiff(
            Callback(oldList, newList)
        )

        currentList = newList

        result.dispatchUpdatesTo(this)
    }

    private data class Header(
        val moreItemCount: Int = 0
    ) {
        val hasMoreItem = moreItemCount > 0
    }

    private data class Item(
        val lectureInfo: LectureInformation,
        val isVisibleDivider: Boolean
    )

    private object EmptyItem

    private object ShowAll

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
                oldItem is Item && newItem is Item -> oldItem.lectureInfo.id == newItem.lectureInfo.id
                oldItem === EmptyItem && newItem === EmptyItem -> true
                oldItem === ShowAll && newItem === ShowAll -> true
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

        class LectureInformationViewHolder(
            val binding: ItemSmallLectureInformationBinding
        ) : ViewHolder(binding.root)

        class TextViewHolder(
            val binding: ItemTextBinding
        ) : ViewHolder(binding.root)

        class ShowAllViewHolder(
            val binding: ItemSmallShowAllBinding
        ) : ViewHolder(binding.root)
    }
}
