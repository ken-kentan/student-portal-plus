package jp.kentan.studentportalplus.ui.adapter

import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.Typeface
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.Notice
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.notice_small_view.view.*


class NoticeAdapter(private val context: Context, private val listener: NoticeAdapter.Listener) :
        ListAdapter<Notice, NoticeAdapter.ViewHolder>(Notice.DIFF_CALLBACK),
        Observer<List<Notice>> {
    override fun onChanged(t: List<Notice>?) {
        submitList(t)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.notice_small_view, parent, false)

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
            view.setOnClickListener {
                listener.onClick(data)
            }
            view.created_date_text.text = data.createdDate.toShortString()
            view.title_text.text = data.title

            if (data.isFavorite) {
                view.favorite_icon.setImageResource(R.drawable.ic_favorite_on)
            } else {
                view.favorite_icon.setImageResource(R.drawable.ic_favorite_off)
            }

            if (data.hasRead) {
                view.created_date_text.setTypeface(null, Typeface.NORMAL)
                view.title_text.setTypeface(null, Typeface.NORMAL)
            } else {
                view.created_date_text.setTypeface(null, Typeface.BOLD)
                view.title_text.setTypeface(null, Typeface.BOLD)
            }

            view.favorite_icon.setOnClickListener{
                listener.onUpdateFavorite(data, !data.isFavorite)
            }
        }
    }

    interface Listener{
        fun onUpdateFavorite(data: Notice, isFavorite: Boolean)
        fun onClick(data: Notice)
    }
}