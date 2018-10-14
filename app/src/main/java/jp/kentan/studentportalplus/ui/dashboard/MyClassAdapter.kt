package jp.kentan.studentportalplus.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.databinding.ItemSmallMyClassBinding

class MyClassAdapter(
        private val layoutInflater: LayoutInflater,
        private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<MyClassAdapter.ViewHolder>() {

    private var currentList: List<MyClass> = emptyList()

    init {
        setHasStableIds(true)
    }

    fun submitList(newList: List<MyClass>) {
        val oldList = currentList

        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        })

        currentList = newList

        result.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = currentList.size

    override fun getItemId(position: Int) = currentList[position].id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemSmallMyClassBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_small_my_class, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], currentList.lastIndex <= position)
    }

    inner class ViewHolder(
            private val binding: ItemSmallMyClassBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MyClass, isLastItem: Boolean) {
            binding.data = data
            binding.layout.setOnClickListener { onClick(data.id) }
            binding.divider.isVisible = !isLastItem
        }
    }
}