package jp.kentan.studentportalplus.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.Notice
import jp.kentan.studentportalplus.ui.NoticeActivity
import jp.kentan.studentportalplus.ui.adapter.NoticeAdapter
import jp.kentan.studentportalplus.ui.viewmodel.NoticeFragmentViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.AnimationHelper
import kotlinx.android.synthetic.main.fragment_notice.*
import org.jetbrains.anko.support.v4.startActivity
import javax.inject.Inject


class NoticeFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notice, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val context  = requireContext()
        val activity = requireActivity()

        val viewModel = ViewModelProvider(activity, viewModelFactory).get(NoticeFragmentViewModel::class.java)

        val noticeAdapter = NoticeAdapter(context, NoticeAdapter.TYPE_NORMAL, object : NoticeAdapter.Listener{
            override fun onUpdateFavorite(data: Notice, isFavorite: Boolean) { }

            override fun onClick(data: Notice) {
                viewModel.updateNotice(data.copy(hasRead = true))
                startActivity<NoticeActivity>("id" to data.id, "title" to data.title)
            }
        })

        viewModel.getNotices().observe(this, Observer {
            noticeAdapter.submitList(it)

            if (it == null || it.isEmpty()) {
                if (text.visibility == View.GONE) {
                    text.startAnimation(AnimationHelper.fadeIn(text))
                }
            } else {
                if (text.visibility == View.VISIBLE) {
                    text.startAnimation(AnimationHelper.fadeOut(text))
                }
            }
        })

        text.text = getString(R.string.msg_not_found, getString(R.string.name_notice))

        initRecyclerView(recycler_view, noticeAdapter)

        activity.onAttachFragment(this)
    }

    private fun initRecyclerView(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = adapter
        view.setHasFixedSize(true)
    }

    companion object {
        var instance: NoticeFragment? = null
            private set
            get() {
                if (field == null) {
                    field = NoticeFragment()
                }

                return field
            }
    }
}
