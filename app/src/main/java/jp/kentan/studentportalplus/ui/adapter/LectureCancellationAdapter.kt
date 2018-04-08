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
        private val viewType: Int,
        private val listener: Listener,
        private val maxItemCount: Int = -1) :
        ListAdapter<LectureCancellation, LectureCancellationAdapter.ViewHolder>(LectureCancellation.DIFF_CALLBACK) {

    companion object {
        const val TYPE_NORMAL = 0
        const val TYPE_SMALL  = 1
    }

    private var isGoneLastSeparator = false

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun getItemViewType(position: Int) = viewType

    override fun submitList(list: List<LectureCancellation>?) {
        if (maxItemCount > 0) {
            isGoneLastSeparator = (list?.size ?: 0) <= maxItemCount

            super.submitList(list?.take(maxItemCount))
        } else {
            super.submitList(list)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        val layoutId = if (viewType == TYPE_NORMAL) R.layout.list_lecture else R.layout.list_small_lecture

        val view = layoutInflater.inflate(layoutId, parent, false)

        return ViewHolder(view, viewType, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (viewType == TYPE_SMALL && isGoneLastSeparator) {
            holder.separator.visibility = if (position == itemCount-1) View.GONE else View.VISIBLE
        }

        holder.bindTo(getItem(position))
    }

    class ViewHolder(
            private val view: View,
            private val viewType: Int,
            private val listener: Listener) : RecyclerView.ViewHolder(view) {

        val separator: View = view.separator

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
        fun onClick(data: LectureCancellation)
    }
}