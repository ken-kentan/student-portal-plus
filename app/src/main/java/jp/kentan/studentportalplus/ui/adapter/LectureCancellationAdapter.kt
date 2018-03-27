package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.list_small_lecture.view.*


class LectureCancellationAdapter(private val context: Context, private val listener: Listener) :
        ListAdapter<LectureCancellation, LectureCancellationAdapter.ViewHolder>(LectureCancellation.DIFF_CALLBACK) {

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

        fun bindTo(data: LectureCancellation) {
            view.date_text.text    = data.createdDate.toShortString()
            view.subject_text.text = data.subject
            view.instructor_text.text  = data.detailText

            if (data.hasRead) {
                view.date_text.typeface    = Typeface.DEFAULT
                view.subject_text.typeface = Typeface.DEFAULT
                view.instructor_text.typeface  = Typeface.DEFAULT
            } else {
                view.date_text.typeface    = Typeface.DEFAULT_BOLD
                view.subject_text.typeface = Typeface.DEFAULT_BOLD
                view.instructor_text.typeface  = Typeface.DEFAULT_BOLD
            }

            view.setOnClickListener { listener.onClick(data) }
        }
    }

    interface Listener{
        fun onClick(data: LectureCancellation)
    }
}