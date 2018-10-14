package jp.kentan.studentportalplus.ui.timetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassWeek
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.databinding.ItemEmptyMyClassBinding
import jp.kentan.studentportalplus.databinding.ItemGridMyClassBinding
import jp.kentan.studentportalplus.databinding.ItemListMyClassBinding
import java.util.*
import kotlin.math.min

class MyClassAdapter(
        private val layoutInflater: LayoutInflater,
        private val onClick: (Long) -> Unit,
        private val onAddClick: (ClassWeek, Int) -> Unit
) : ListAdapter<MyClass, MyClassAdapter.ViewHolder>(MyClass.DIFF_CALLBACK) {

    private companion object {
        const val GRID_TYPE = 0
        const val EMPTY_TYPE = 1
        const val LIST_TYPE = 2

        val PERIOD_MINUTES = intArrayOf(8 * 60 + 50, 10 * 60 + 30, 12 * 60 + 50, 14 * 60 + 30, 16 * 60 + 10, 17 * 60 + 50, 19 * 60 + 30)
    }

    var isGridLayout = true
        set(value) {
            submitList(null)
            field = value
        }
    private val calender by lazy(LazyThreadSafetyMode.NONE) { Calendar.getInstance() }

    init {
        setHasStableIds(true)
    }

    fun updateCalender() {
        calender.timeInMillis = System.currentTimeMillis()
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun getItemViewType(position: Int) = if (!isGridLayout) {
        LIST_TYPE
    } else if (getItemId(position) >= 0) {
        GRID_TYPE
    } else {
        EMPTY_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyClassAdapter.ViewHolder {
        when (viewType) {
            GRID_TYPE -> {
                val binding: ItemGridMyClassBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.item_grid_my_class, parent, false)

                return GridViewHolder(binding)
            }
            EMPTY_TYPE -> {
                val binding: ItemEmptyMyClassBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.item_empty_my_class, parent, false)

                return EmptyViewHolder(binding)
            }
            LIST_TYPE -> {
                val binding: ItemListMyClassBinding =
                        DataBindingUtil.inflate(layoutInflater, R.layout.item_list_my_class, parent, false)

                return ListViewHolder(binding)
            }
            else -> throw IllegalArgumentException("viewType($viewType) is not supported.")
        }
    }

    override fun onBindViewHolder(holder: MyClassAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))

        if (isGridLayout) {
            holder.setMask(calcMaskGuidelinePercent(position))
        }
    }

    private fun calcMaskGuidelinePercent(position: Int): Float {
        val day = position % 5 + 2

        val dayOfWeek = calender.get(Calendar.DAY_OF_WEEK)

        if (day < dayOfWeek || dayOfWeek == Calendar.SUNDAY) {
            return 1f
        } else if (day == dayOfWeek) {
            val period = position / 5
            val minutes = calender.get(Calendar.MINUTE) + calender.get(Calendar.HOUR_OF_DAY) * 60

            val diff = (minutes - PERIOD_MINUTES[period])

            if (diff > 0) {
                return min(diff / 90f, 1f)
            }
        }

        return 0f
    }

    inner class GridViewHolder(
            private val binding: ItemGridMyClassBinding
    ) : ViewHolder(binding.root) {
        override fun bind(data: MyClass) {
            binding.data = data
            binding.layout.setOnClickListener { onClick(data.id) }
        }

        override fun setMask(ratio: Float) {
            if (ratio > 0f) {
                binding.guideline.setGuidelinePercent(ratio)
                binding.maskGroup.isVisible = true
            } else {
                binding.maskGroup.isVisible = false
            }
        }
    }

    inner class EmptyViewHolder(
            private val binding: ItemEmptyMyClassBinding
    ) : ViewHolder(binding.root) {
        override fun bind(data: MyClass) {
            binding.layout.setOnClickListener { onAddClick(data.week, data.period) }
        }

        override fun setMask(ratio: Float) {
            if (ratio > 0f) {
                binding.guideline.setGuidelinePercent(ratio)
                binding.maskGroup.isVisible = true
            } else {
                binding.maskGroup.isVisible = false
            }
        }
    }

    inner class ListViewHolder(
            private val binding: ItemListMyClassBinding
    ) : ViewHolder(binding.root) {
        override fun bind(data: MyClass) {
            binding.data = data
            binding.layout.setOnClickListener { onClick(data.id) }
        }

        override fun setMask(ratio: Float) { }
    }

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: MyClass)
        abstract fun setMask(ratio: Float)
    }
}