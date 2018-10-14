package jp.kentan.studentportalplus.ui.lectureinfo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.databinding.ItemLectureBinding

class LectureInfoAdapter(
        private val layoutInflater: LayoutInflater,
        private val onClick: (Long) -> Unit
) : ListAdapter<LectureInformation, LectureInfoAdapter.ViewHolder>(LectureInformation.DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemLectureBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_lecture, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
            private val binding: ItemLectureBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: LectureInformation) {
            binding.apply {
                setData(data)
                layout.setOnClickListener { onClick(data.id) }
            }
        }
    }
}