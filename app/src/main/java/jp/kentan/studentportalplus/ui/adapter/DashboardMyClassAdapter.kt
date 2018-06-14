package jp.kentan.studentportalplus.ui.adapter

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.MyClass
import kotlinx.android.synthetic.main.list_small_my_class.view.*
import org.jetbrains.anko.backgroundColor

/**
 * MyClassAdapter on MainThread
 *
 * @see MyClassAdapter
 */
class DashboardMyClassAdapter(
        private val context: Context,
        private val onClick: (data: MyClass) -> Unit = {}
) : RecyclerView.Adapter<DashboardMyClassAdapter.ViewHolder>() {

    private var currentList: List<MyClass> = emptyList()

    init {
        setHasStableIds(true)
    }

    override fun getItemCount() = currentList.size

    override fun getItemId(position: Int) = currentList[position].id

    fun submitList(newList: List<MyClass>) {
        val oldList = currentList

        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int { return oldList.size }

            override fun getNewListSize(): Int { return newList.size }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return MyClass.DIFF_CALLBACK.areItemsTheSame(
                        oldList[oldItemPosition], newList[newItemPosition]
                )
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return MyClass.DIFF_CALLBACK.areContentsTheSame(
                        currentList[oldItemPosition], newList[newItemPosition]
                )
            }
        })

        currentList = newList

        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.list_small_my_class, parent, false)

        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.separator.visibility = if (position >= itemCount-1) View.GONE else View.VISIBLE
        holder.bindTo(currentList[position])
    }

    class ViewHolder(
            private val view: View,
            private val onClick: (data: MyClass) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        val separator: View = view.separator

        fun bindTo(data: MyClass) {
            view.color_header.backgroundColor = data.color
            view.subject.text    = data.subject
            view.instructor.text = data.instructor
            view.location.text   = data.location
            view.period.text     = data.period.toString()

            view.instructor.visibility = if (data.instructor.isNotBlank()) View.VISIBLE else View.GONE

            view.setOnClickListener { onClick(data) }
        }
    }
}