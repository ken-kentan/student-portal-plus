package jp.kentan.studentportalplus.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.Lecture
import jp.kentan.studentportalplus.databinding.ItemSmallLectureBinding

class LectureAdapter(
        private val layoutInflater: LayoutInflater,
        private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<LectureAdapter.ViewHolder>() {

    private var currentList: List<Lecture> = emptyList()
    private var isInvisibleLastDivider = false

    init {
        setHasStableIds(true)
    }

    fun submitList(newList: List<Lecture>, isInvisibleLastDivider: Boolean) {
        val oldList = currentList

        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition] &&
                        newList.lastIndex != newItemPosition // Update last divider
            }
        })

        currentList = newList
        this.isInvisibleLastDivider = isInvisibleLastDivider

        result.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = currentList.size

    override fun getItemId(position: Int) = currentList[position].id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemSmallLectureBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_small_lecture, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], currentList.lastIndex <= position)
    }

    inner class ViewHolder(
            private val binding: ItemSmallLectureBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Lecture, isLastItem: Boolean) {
            binding.apply {
                setData(data)
                layout.setOnClickListener { onClick(data.id) }
                divider.isVisible = !(isLastItem && isInvisibleLastDivider)
            }
        }
    }
}