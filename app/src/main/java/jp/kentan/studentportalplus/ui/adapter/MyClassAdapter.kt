package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.model.MyClass
import kotlinx.android.synthetic.main.grid_my_class.view.*
import kotlinx.android.synthetic.main.grid_my_class_empty.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk25.coroutines.onClick


class MyClassAdapter(
        private val context: Context,
        private val viewType: Int,
        private val listener: Listener) :
        ListAdapter<MyClass, MyClassAdapter.ViewHolder>(MyClass.DIFF_CALLBACK) {

    companion object {
        private const val TYPE_EMPTY = -1
        const val TYPE_GRID  = 0
        const val TYPE_LIST  = 1
        const val TYPE_SMALL = 2
    }

    init {
        setHasStableIds(true)

        if (viewType != TYPE_GRID && viewType != TYPE_LIST && viewType != TYPE_SMALL) {
            throw IllegalArgumentException("Invalid ViewType: $viewType")
        }
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun getItemViewType(position: Int) = if (getItemId(position) < 0) TYPE_EMPTY else viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)

        val layoutId = when (viewType) {
            TYPE_GRID  -> R.layout.grid_my_class
            TYPE_LIST  -> R.layout.grid_my_class
            TYPE_EMPTY -> R.layout.grid_my_class_empty
            else       -> R.layout.grid_my_class
        }

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

        fun bindTo(data: MyClass) {
            when (viewType) {
                TYPE_EMPTY -> {
                    view.add_button.setOnClickListener {
                        listener.onAddClick(data.period, data.week)
                    }
                }
                else -> {
                    view.subject_text.text    = data.subject
                    view.location_text.text   = data.location
                    view.instructor_text.text = data.instructor

                    view.layout.backgroundColor = data.color

                    view.layout.onClick {
                        listener.onClick(data)
                    }
                }
            }
        }
    }

    interface Listener{
        fun onClick(data: MyClass)
        fun onAddClick(period: Int, week: ClassWeekType)
    }
}