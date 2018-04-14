package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.list_small_lecture.view.*

/**
 * LectureInformationAdapter on MainThread
 *
 * @see LectureInformationAdapter
 */
class DashboardLectureInformationAdapter(
        private val context: Context,
        private val maxItemCount: Int,
        private val onClick: (data: LectureInformation) -> Unit = {}) :
RecyclerView.Adapter<DashboardLectureInformationAdapter.ViewHolder>() {

    private var currentList: List<LectureInformation> = emptyList()
    private var isOverMaxItemCount: Boolean = false

    init {
        setHasStableIds(true)
    }

    override fun getItemCount() = currentList.size

    override fun getItemId(position: Int) = currentList.get(position).id

    fun submitList(list: List<LectureInformation>) {
        val oldList = currentList
        val newList = list.take(maxItemCount)

        isOverMaxItemCount = list.size > maxItemCount

        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int { return oldList.size }

            override fun getNewListSize(): Int { return newList.size }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return LectureInformation.DIFF_CALLBACK.areItemsTheSame(
                        oldList.get(oldItemPosition), newList.get(newItemPosition)
                )
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return LectureInformation.DIFF_CALLBACK.areContentsTheSame(
                        currentList.get(oldItemPosition), newList.get(newItemPosition)
                )
            }
        })

        currentList = newList

        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.list_small_lecture, parent, false)

        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.separator.visibility = if (!isOverMaxItemCount && position >= itemCount-1) View.GONE else View.VISIBLE
        holder.bindTo(currentList.get(position))
    }

    class ViewHolder(
            private val view: View,
            private val onClick: (data: LectureInformation) -> Unit) : RecyclerView.ViewHolder(view) {

        val separator: View = view.separator

        fun bindTo(data: LectureInformation) {
            view.date.text    = data.updatedDate.toShortString()
            view.subject.text = data.subject
            view.detail.text  = data.detailText

            if (data.hasRead) {
                view.date.typeface    = Typeface.DEFAULT
                view.subject.typeface = Typeface.DEFAULT
                view.detail.typeface  = Typeface.DEFAULT
            } else {
                view.date.typeface    = Typeface.DEFAULT_BOLD
                view.subject.typeface = Typeface.DEFAULT_BOLD
                view.detail.typeface  = Typeface.DEFAULT_BOLD
            }

            view.setOnClickListener { onClick(data) }
        }
    }
}