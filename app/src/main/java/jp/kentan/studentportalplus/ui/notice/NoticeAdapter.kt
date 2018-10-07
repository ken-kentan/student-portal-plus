package jp.kentan.studentportalplus.ui.notice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.databinding.ItemNoticeBinding

class NoticeAdapter(
        private val layoutInflater: LayoutInflater,
        private val onClick: (Long) -> Unit,
        private val onFavoriteClick: (Notice) -> Unit
) : ListAdapter<Notice, NoticeAdapter.ViewHolder>(Notice.DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemNoticeBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_notice, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
            private val binding: ItemNoticeBinding
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