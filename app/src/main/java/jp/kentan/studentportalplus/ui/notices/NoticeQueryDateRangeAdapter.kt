package jp.kentan.studentportalplus.ui.notices

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import jp.kentan.studentportalplus.data.vo.NoticeQuery

class NoticeQueryDateRangeAdapter(context: Context) :
    ArrayAdapter<NoticeQuery.DateRange>(context, 0, NoticeQuery.DateRange.values()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup) =
        createView(position, convertView, parent)

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) =
        createView(position, convertView, parent)

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_list_item_1,
            parent,
            false
        )
        val textView = view as TextView

        val dateRange = requireNotNull(getItem(position))
        textView.setText(dateRange.resId)

        return textView
    }
}
