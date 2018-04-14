package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.model.MyClass
import kotlinx.android.synthetic.main.grid_my_class.view.*
import kotlinx.android.synthetic.main.grid_my_class_empty.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick


class MyClassAdapter(
        private val context: Context,
        private var viewType: Int,
        private val onClick: (data: MyClass) -> Unit,
        private val onAddClick: (period: Int, week: ClassWeekType) -> Unit
) : ListAdapter<MyClass, MyClassAdapter.ViewHolder>(MyClass.DIFF_CALLBACK) {

    companion object {
        private const val TYPE_EMPTY = -1
        const val TYPE_GRID  = 0
        const val TYPE_LIST  = 1
    }

    init {
        setHasStableIds(true)

        if (viewType != TYPE_GRID && viewType != TYPE_LIST) {
            throw IllegalArgumentException("Invalid ViewType: $viewType")
        }
    }

    fun setViewType(viewType: Int) {
        if (viewType != TYPE_GRID && viewType != TYPE_LIST) {
            throw IllegalArgumentException("Invalid ViewType: $viewType")
        }
        this.viewType = viewType
        submitList(null)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun getItemViewType(position: Int) = if (getItemId(position) < 0) TYPE_EMPTY else viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        val layoutId = when (viewType) {
            TYPE_EMPTY -> R.layout.grid_my_class_empty
            TYPE_GRID  -> R.layout.grid_my_class
            TYPE_LIST  -> R.layout.list_my_class
            else       -> R.layout.list_my_class
        }

        val view = layoutInflater.inflate(layoutId, parent, false)

        return ViewHolder(view, viewType, onClick, onAddClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class ViewHolder(
            private val view: View,
            private val viewType: Int,
            private val onClick: (data: MyClass) -> Unit,
            private val onAddClick: (period: Int, week: ClassWeekType) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        fun bindTo(data: MyClass) {
            if (viewType == TYPE_EMPTY) {
                view.add_button.setOnClickListener { onAddClick(data.period, data.week) }
                return
            }
            when (viewType) {
               TYPE_LIST -> {
                   view.find<ImageView>(R.id.user_icon).setImageResource(
                           if (data.isUser) R.drawable.ic_lock_off else R.drawable.ic_lock_on
                   )
                   view.find<View>(R.id.color_header).backgroundColor = data.color
                   view.find<TextView>(R.id.day_and_period).text = formatDayAndPeriod(data)
               }
                TYPE_GRID -> {
                    view.location.text     = data.location
                    view.layout.backgroundColor = data.color
                }
            }

            view.subject.text    = data.subject
            view.instructor.text = data.instructor

            view.layout.onClick { onClick(data) }
        }

        private companion object {
            fun formatDayAndPeriod(data: MyClass): String {
                return if (data.week.hasPeriod()) {
                    data.week.displayName + data.period
                } else {
                    data.week.displayName
                }
            }
        }
    }
}