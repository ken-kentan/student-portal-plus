package jp.kentan.studentportalplus.ui.lectureinformation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.databinding.ItemLectureInformationBinding
import jp.kentan.studentportalplus.databinding.ItemTextBinding
import jp.kentan.studentportalplus.util.executeAfter

class LectureInformationAdapter(
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<LectureInformationAdapter.ViewHolder>() {

    private val differ = AsyncListDiffer<Any>(this, DiffCallback)

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is LectureInformation -> R.layout.item_lecture_information
            EmptyItem -> R.layout.item_text
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.item_lecture_information -> ViewHolder.LectureInformationViewHolder(
                ItemLectureInformationBinding.inflate(inflater, parent, false)
            )
            R.layout.item_text -> ViewHolder.TextViewHolder(
                ItemTextBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.LectureInformationViewHolder -> holder.binding.executeAfter {
                val lectureInfo = differ.currentList[position] as LectureInformation

                data = lectureInfo
                root.setOnClickListener { onItemClick(lectureInfo.id) }
            }
            is ViewHolder.TextViewHolder -> holder.binding.executeAfter {
                textView.setText(R.string.text_empty_lecture_information)
            }
        }
    }

    fun submitList(newList: List<LectureInformation>) {
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
                oldItem is LectureInformation && newItem is LectureInformation -> oldItem.id == newItem.id
                else -> false
            }
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is LectureInformation && newItem is LectureInformation -> oldItem == newItem
                else -> true
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class LectureInformationViewHolder(
            val binding: ItemLectureInformationBinding
        ) : ViewHolder(binding.root)

        class TextViewHolder(
            val binding: ItemTextBinding
        ) : ViewHolder(binding.root)
    }
}