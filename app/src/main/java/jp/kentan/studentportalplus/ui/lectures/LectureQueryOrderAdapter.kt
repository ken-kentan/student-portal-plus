package jp.kentan.studentportalplus.ui.lectures

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import jp.kentan.studentportalplus.data.vo.LectureQuery

class LectureQueryOrderAdapter(context: Context) :
    ArrayAdapter<LectureQuery.Order>(context, 0, LectureQuery.Order.values()) {

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

        val order = requireNotNull(getItem(position))
        textView.setText(order.resId)

        return textView
    }
}
