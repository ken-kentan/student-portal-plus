package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.list_notice.view.*


class NoticeAdapter(
        private val context: Context,
        private val viewType: Int,
        private val listener: Listener,
        private val maxItemCount: Int = -1) :
        ListAdapter<Notice, NoticeAdapter.ViewHolder>(Notice.DIFF_CALLBACK) {

    companion object {
        const val TYPE_NORMAL = 0
        const val TYPE_SMALL  = 1
    }

    init {
        setHasStableIds(true)

        if (viewType != TYPE_NORMAL && viewType != TYPE_SMALL) {
            throw IllegalArgumentException("Invalid ViewType: $viewType")
        }
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun getItemViewType(position: Int) = viewType

    override fun submitList(list: List<Notice>?) {
        if (maxItemCount > 0) {
            super.submitList(list?.take(maxItemCount))
        } else {
            super.submitList(list)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        val layoutId = if (viewType == TYPE_NORMAL) R.layout.list_notice else R.layout.list_small_notice

        val view = layoutInflater.inflate(layoutId, parent, false)

        return ViewHolder(view, viewType, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class ViewHolder(
            private val view: View,
            private val viewType: Int,
            private val listener: Listener) : RecyclerView.ViewHolder(view) {

        fun bindTo(data: Notice) {
            view.date_text.text = data.createdDate.toShortString()
            view.subject_text.text          = data.title

            if (data.isFavorite) {
                view.favorite_icon.setImageResource(R.drawable.ic_favorite_on)
            } else {
                view.favorite_icon.setImageResource(R.drawable.ic_favorite_off)
            }

            if (data.hasRead) {
                view.date_text.typeface = Typeface.DEFAULT
                view.subject_text.typeface          = Typeface.DEFAULT
            } else {
                view.date_text.typeface = Typeface.DEFAULT_BOLD
                view.subject_text.typeface          = Typeface.DEFAULT_BOLD
            }

            view.setOnClickListener {
                listener.onClick(data)
            }
            view.favorite_icon.setOnClickListener{
                listener.onUpdateFavorite(data, !data.isFavorite)
            }

            if (viewType == TYPE_NORMAL) {
                view.instructor_text.text = data.detailText ?: data.link
            }
        }
    }

    interface Listener{
        fun onUpdateFavorite(data: Notice, isFavorite: Boolean)
        fun onClick(data: Notice)
    }
}