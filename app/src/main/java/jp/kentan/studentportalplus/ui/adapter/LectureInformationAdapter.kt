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
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.list_lecture.view.*


class LectureInformationAdapter(
        private val context: Context,
        private val viewType: Int,
        private val listener: Listener) :
        ListAdapter<LectureInformation, LectureInformationAdapter.ViewHolder>(LectureInformation.DIFF_CALLBACK) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        val layoutId = if (viewType == TYPE_NORMAL) R.layout.list_lecture else R.layout.list_small_lecture

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

        fun bindTo(data: LectureInformation) {
            view.date_text.text    = data.updatedDate.toShortString()
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

            if (viewType == TYPE_NORMAL) {
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
            }

            view.setOnClickListener { listener.onClick(data) }
        }
    }

    interface Listener{
        fun onClick(data: LectureInformation)
    }
}