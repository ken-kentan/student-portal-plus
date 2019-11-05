package jp.kentan.studentportalplus.ui.notice

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.databinding.ItemNoticeBinding
import jp.kentan.studentportalplus.databinding.ItemTextBinding
import jp.kentan.studentportalplus.util.executeAfter

class NoticeAdapter(
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {

    private val differ = AsyncListDiffer<Any>(this, DiffCallback)

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is Notice -> R.layout.item_notice
            EmptyItem -> R.layout.item_text
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.item_notice -> ViewHolder.NoticeViewHolder(
                ItemNoticeBinding.inflate(inflater, parent, false)
            )
            R.layout.item_text -> ViewHolder.TextViewHolder(
                ItemTextBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.NoticeViewHolder -> holder.binding.executeAfter {
                val notice = differ.currentList[position] as Notice

                data = notice
                root.setOnClickListener { onItemClick(notice.id) }
            }
            is ViewHolder.TextViewHolder -> holder.binding.executeAfter {
                textView.setText(R.string.text_not_found_notice)
            }
        }
    }

    fun submitList(newList: List<Notice>) {
        if (newList.isEmpty()) {
            differ.submitList(listOf(EmptyItem))
            return
        }

        differ.submitList(newList)
    }

    object EmptyItem

    object DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem === EmptyItem && newItem === EmptyItem -> true
                oldItem is Notice && newItem is Notice -> oldItem.id == newItem.id
                else -> false
            }
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is Notice && newItem is Notice -> oldItem == newItem
                else -> true
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class NoticeViewHolder(
            val binding: ItemNoticeBinding
        ) : ViewHolder(binding.root)

        class TextViewHolder(
            val binding: ItemTextBinding
        ) : ViewHolder(binding.root)
    }
}