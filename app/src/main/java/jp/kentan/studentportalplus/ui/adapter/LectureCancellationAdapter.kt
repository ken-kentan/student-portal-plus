package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.list_lecture.view.*


class LectureCancellationAdapter(
        private val context: Context,
        private val listener: Listener) :
        ListAdapter<LectureCancellation, LectureCancellationAdapter.ViewHolder>(LectureCancellation.DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.list_lecture, parent, false)

        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class ViewHolder(
            private val view: View,
            private val listener: Listener) : RecyclerView.ViewHolder(view) {

        fun bindTo(data: LectureCancellation) {
            view.date.text    = data.createdDate.toShortString()
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

            when (data.attend) {
                LectureAttendType.PORTAL, LectureAttendType.USER -> {
                    view.attend_icon.setImageResource(R.drawable.ic_lecture_attend)
                }
                LectureAttendType.SIMILAR -> {
                    view.attend_icon.setImageResource(R.drawable.ic_lecture_attend_similar)
                }
                else -> {
                    view.attend_icon.setImageResource(R.drawable.ic_lecture_attend_not)
                }
            }

            view.setOnClickListener { listener.onClick(data) }
        }
    }

    interface Listener{
        fun onClick(data: LectureCancellation)
    }
}