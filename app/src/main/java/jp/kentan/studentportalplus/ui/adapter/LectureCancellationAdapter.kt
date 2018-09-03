package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.databinding.ItemLectureBinding


class LectureCancellationAdapter(
        private val context: Context,
        private val onClick: (data: LectureCancellation) -> Unit = {}) :
        ListAdapter<LectureCancellation, LectureCancellationAdapter.ViewHolder>(LectureCancellation.DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ItemLectureBinding>(inflater, R.layout.item_lecture, parent, false)

        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
            private val binding: ItemLectureBinding,
            private val onClick: (data: LectureCancellation) -> Unit) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: LectureCancellation) = binding.apply {
            lecture = data
            setOnClickListener { onClick(data) }
        }
    }
}