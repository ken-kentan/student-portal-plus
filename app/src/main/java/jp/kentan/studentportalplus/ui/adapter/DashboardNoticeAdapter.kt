package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.list_notice.view.*

/**
 * NoticeAdapter on MainThread
 *
 * @see NoticeAdapter
 */
class DashboardNoticeAdapter(
        private val context: Context,
        private val maxItemCount: Int,
        private val onClick: (data: Notice) -> Unit,
        private val onClickFavorite: (data: Notice) -> Unit
) : RecyclerView.Adapter<DashboardNoticeAdapter.ViewHolder>() {

    private var currentList: List<Notice> = emptyList()
    private var isOverMaxItemCount: Boolean = false

    init {
        setHasStableIds(true)
    }

    override fun getItemCount() = currentList.size

    override fun getItemId(position: Int) = currentList[position].id

    fun submitList(list: List<Notice>) {
        val oldList = currentList
        val newList = list.take(maxItemCount)

        isOverMaxItemCount = list.size > maxItemCount

        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int { return oldList.size }

            override fun getNewListSize(): Int { return newList.size }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return Notice.DIFF_CALLBACK.areItemsTheSame(
                        oldList[oldItemPosition], newList[newItemPosition]
                )
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return Notice.DIFF_CALLBACK.areContentsTheSame(
                        currentList[oldItemPosition], newList[newItemPosition]
                )
            }
        })

        currentList = newList

        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.list_small_notice, parent, false)

        return ViewHolder(view, onClick, onClickFavorite)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(currentList[position])
    }

    class ViewHolder(
            private val view: View,
            private val onClick: (data: Notice) -> Unit,
            private val onClickFavorite: (data: Notice) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        fun bindTo(data: Notice) {
            view.date.text = data.createdDate.toShortString()
            view.subject.text          = data.title

            if (data.isFavorite) {
                view.favorite_icon.setImageResource(R.drawable.ic_favorite_on)
            } else {
                view.favorite_icon.setImageResource(R.drawable.ic_favorite_off)
            }

            if (data.hasRead) {
                view.date.typeface = Typeface.DEFAULT
                view.subject.typeface = Typeface.DEFAULT
            } else {
                view.date.typeface = Typeface.DEFAULT_BOLD
                view.subject.typeface = Typeface.DEFAULT_BOLD
            }

            view.setOnClickListener { onClick(data) }
            view.favorite_icon.setOnClickListener { onClickFavorite(data) }
        }
    }
}