package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.LectureInformation
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.list_small_lecture.view.*


class LectureInformationAdapter(
        private val context: Context,
        private val listener: Listener) :
        ListAdapter<LectureInformation, LectureInformationAdapter.ViewHolder>(LectureInformation.DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.list_small_lecture, parent, false)

        return ViewHolder(view, viewType, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class ViewHolder(
            private val view: View,
            private val viewType: Int,
            private val listener: Listener) : RecyclerView.ViewHolder(view) {

        fun bindTo(data: LectureInformation) {
            view.date_text.text    = data.updatedDate.toShortString()
            view.subject_text.text = data.subject
            view.detail_text.text  = data.detailText

            if (data.hasRead) {
                view.date_text.typeface    = Typeface.DEFAULT
                view.subject_text.typeface = Typeface.DEFAULT
                view.detail_text.typeface  = Typeface.DEFAULT
            } else {
                view.date_text.typeface    = Typeface.DEFAULT_BOLD
                view.subject_text.typeface = Typeface.DEFAULT_BOLD
                view.detail_text.typeface  = Typeface.DEFAULT_BOLD
            }

            view.setOnClickListener { listener.onClick(data) }
        }
    }

    interface Listener{
        fun onClick(data: LectureInformation)
    }
}