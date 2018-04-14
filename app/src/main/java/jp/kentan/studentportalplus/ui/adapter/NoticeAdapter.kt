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
        private val listener: Listener) :
        ListAdapter<Notice, NoticeAdapter.ViewHolder>(Notice.DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        val view = layoutInflater.inflate(R.layout.list_notice, parent, false)

        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class ViewHolder(
            private val view: View,
            private val listener: Listener) : RecyclerView.ViewHolder(view) {

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

            view.setOnClickListener {
                listener.onClick(data)
            }
            view.favorite_icon.setOnClickListener{
                listener.onUpdateFavorite(data, !data.isFavorite)
            }

            view.instructor.text = data.detailText ?: data.link
        }
    }

    interface Listener{
        fun onUpdateFavorite(data: Notice, isFavorite: Boolean)
        fun onClick(data: Notice)
    }
}