package jp.kentan.studentportalplus.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.Notice
import jp.kentan.studentportalplus.ui.MainActivity
import jp.kentan.studentportalplus.ui.adapter.NoticeAdapter
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


class DashboardFragment : Fragment() {

    private var noticeAdapter: NoticeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: throw NullPointerException("Null context")

        if (context is MainActivity) {
            val manager = context.portalDataManager ?: return

            noticeAdapter = NoticeAdapter(context, object : NoticeAdapter.Listener{
                override fun onUpdateFavorite(data: Notice, isFavorite: Boolean) {
                    manager.update(data.copy(isFavorite = isFavorite))
                }

                override fun onClick(data: Notice) {
                    manager.update(data.copy(hasRead = true))
                }

            })

            manager.noticeLiveData.observe(context, noticeAdapter!!) //TODO
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        async(UI) {
            bindAdapter(view.notice_recycler_view, noticeAdapter)
        }

        return view
    }

    private fun bindAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = adapter
        view.isNestedScrollingEnabled = false
        view.setHasFixedSize(false)
    }

    companion object {
        var instance: DashboardFragment? = null
            private set
            get() {
                if (field == null) {
                    field = DashboardFragment()
                }

                return field
            }
    }
}
