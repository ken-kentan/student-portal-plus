package jp.kentan.studentportalplus.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.databinding.ItemSmallHeaderBinding
import jp.kentan.studentportalplus.databinding.ItemSmallNoticeBinding
import jp.kentan.studentportalplus.databinding.ItemSmallShowAllBinding
import jp.kentan.studentportalplus.util.executeAfter

class NoticeAdapter(
    private val onItemClick: (Long) -> Unit,
    private val onFavoriteClick: (Notice) -> Unit,
    private val onShowAllClick: (() -> Unit)
) : RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {

    private companion object {
        const val MAX_ITEM_COUNT = 3
    }

    private var currentList: List<Any> = listOf(Header)

    override fun getItemCount() = currentList.size

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        Header -> R.layout.item_small_header
        is Notice -> R.layout.item_small_notice
        ShowAll -> R.layout.item_small_show_all
        else -> throw IllegalStateException("Unknown view type at position $position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.item_small_header -> ViewHolder.HeaderViewHolder(
                ItemSmallHeaderBinding.inflate(inflater, parent, false)
            )
            R.layout.item_small_notice -> ViewHolder.NoticeViewHolder(
                ItemSmallNoticeBinding.inflate(inflater, parent, false)
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
                textView.setText(R.string.name_notice)
            }
            is ViewHolder.NoticeViewHolder -> holder.binding.executeAfter {
                val notice = currentList[position] as Notice

                data = notice
                favoriteImageView.setOnClickListener { onFavoriteClick(notice) }
                layout.setOnClickListener { onItemClick(notice.id) }
            }
            is ViewHolder.ShowAllViewHolder -> holder.binding.executeAfter {
                textView.apply {
                    setText(R.string.dashboard_open_notices_action)
                    setOnClickListener { onShowAllClick.invoke() }
                }
            }
        }
    }

    fun submitList(list: List<Notice>) {
        val newList = mutableListOf<Any>()
        val oldList = currentList

        // header
        newList.add(Header)

        // data
        newList.addAll(list.take(MAX_ITEM_COUNT))

        // show all
        newList.add(ShowAll)

        val result = DiffUtil.calculateDiff(
            Callback(oldList, newList)
        )

        currentList = newList

        result.dispatchUpdatesTo(this)
    }


    private object Header

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
                oldItem === Header && newItem === Header -> true
                oldItem is Notice && newItem is Notice -> oldItem.id == newItem.id
                oldItem === ShowAll && newItem === ShowAll -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return when {
                oldItem is Notice && newItem is Notice -> oldItem == newItem
                else -> true
            }
        }
    }


    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class HeaderViewHolder(
            val binding: ItemSmallHeaderBinding
        ) : ViewHolder(binding.root)

        class NoticeViewHolder(
            val binding: ItemSmallNoticeBinding
        ) : ViewHolder(binding.root)

        class ShowAllViewHolder(
            val binding: ItemSmallShowAllBinding
        ) : ViewHolder(binding.root)
    }
}
