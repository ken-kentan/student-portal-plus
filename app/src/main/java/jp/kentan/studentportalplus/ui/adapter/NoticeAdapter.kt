package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.NoticeData
import kotlinx.android.synthetic.main.notice_small_view.view.*


class NoticeAdapter(val context: Context) : ListAdapter<NoticeData, NoticeAdapter.ViewHolder>(NoticeData.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.notice_small_view, parent, false)

        return ViewHolder(context, view, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }


    class ViewHolder(val context: Context, val view: View, val viewType: Int) : RecyclerView.ViewHolder(view) {
        fun bindTo(data: NoticeData) {
            view.title_text.text = data.title
        }
    }
}