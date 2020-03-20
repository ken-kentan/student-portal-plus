package jp.kentan.studentportalplus.view.widget

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.LayoutRes

class MaterialArrayAdapter(
    context: Context,
    @LayoutRes resource: Int,
    var objects: List<String>
) : ArrayAdapter<String>(context, resource, objects) {

    private val returnAllFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults().apply {
            values = objects
            count = objects.size
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }

    override fun getFilter() = returnAllFilter
}
