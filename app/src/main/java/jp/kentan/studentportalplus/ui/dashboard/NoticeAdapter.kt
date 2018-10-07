package jp.kentan.studentportalplus.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.databinding.ItemSmallNoticeBinding

class NoticeAdapter(
        private val layoutInflater: LayoutInflater,
        private val onClick: (Long) -> Unit,
        private val onFavoriteClick: (Notice) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {

    private var currentList: List<Notice> = emptyList()

    init {
        setHasStableIds(true)
    }

    fun submitList(newList: List<Notice>) {
        val oldList = currentList

        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return Notice.DIFF_CALLBACK.areItemsTheSame(
                        oldList[oldItemPosition], newList[newItemPosition]
                )
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return Notice.DIFF_CALLBACK.areContentsTheSame(
                        oldList[oldItemPosition], newList[newItemPosition]
                )
            }
        })

        currentList = newList

        result.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = currentList.size

    override fun getItemId(position: Int) = currentList[position].id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemSmallNoticeBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_small_notice, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(
            private val binding: ItemSmallNoticeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Notice) {
            binding.apply {
                setData(data)
                layout.setOnClickListener { onClick(data.id) }
                favoriteIcon.setOnClickListener { onFavoriteClick(data) }
            }
        }
    }
}