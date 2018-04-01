package jp.kentan.studentportalplus.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.ui.LectureCancellationActivity
import jp.kentan.studentportalplus.ui.LectureInformationActivity
import jp.kentan.studentportalplus.ui.NoticeActivity
import jp.kentan.studentportalplus.ui.adapter.LectureCancellationAdapter
import jp.kentan.studentportalplus.ui.adapter.LectureInformationAdapter
import jp.kentan.studentportalplus.ui.adapter.NoticeAdapter
import jp.kentan.studentportalplus.ui.viewmodel.DashboardFragmentViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.jetbrains.anko.support.v4.startActivity
import javax.inject.Inject


class DashboardFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val context  = requireContext()
        val activity = requireActivity()

        val viewModel = ViewModelProvider(activity, viewModelFactory).get(DashboardFragmentViewModel::class.java)

        val lectureInfoAdapter = LectureInformationAdapter(context, LectureInformationAdapter.TYPE_SMALL, object : LectureInformationAdapter.Listener{
            override fun onClick(data: LectureInformation) {
                viewModel.updateLectureInformation(data.copy(hasRead = true))
                startActivity<LectureInformationActivity>("id" to data.id, "title" to data.subject)
            }
        })

        val lectureCancelAdapter =  LectureCancellationAdapter(context, LectureCancellationAdapter.TYPE_SMALL, object : LectureCancellationAdapter.Listener{
            override fun onClick(data: LectureCancellation) {
                viewModel.updateLectureCancellation(data.copy(hasRead = true))
                startActivity<LectureCancellationActivity>("id" to data.id, "title" to data.subject)
            }
        })

        val noticeAdapter = NoticeAdapter(context, NoticeAdapter.TYPE_SMALL, object : NoticeAdapter.Listener{
            override fun onUpdateFavorite(data: Notice, isFavorite: Boolean) {
                viewModel.updateNotice(data.copy(isFavorite = isFavorite))
            }

            override fun onClick(data: Notice) {
                viewModel.updateNotice(data.copy(hasRead = true))
                startActivity<NoticeActivity>("id" to data.id, "title" to data.title)
            }
        })

        viewModel.getLectureInformations().observe(this, Observer {
            lectureInfoAdapter.submitList(it?.take(3))
            TransitionManager.beginDelayedTransition(dashboard_layout)
        })
        viewModel.getLectureCancellations().observe(this, Observer {
            lectureCancelAdapter.submitList(it?.take(3))
            TransitionManager.beginDelayedTransition(dashboard_layout)
        })
        viewModel.getNotices().observe(this, Observer {
            noticeAdapter.submitList(it?.take(3))
            TransitionManager.beginDelayedTransition(dashboard_layout)
        })

        initRecyclerView(lecture_info_recycler_view, lectureInfoAdapter)
        initRecyclerView(lecture_cancel_recycler_view, lectureCancelAdapter)
        initRecyclerView(notice_recycler_view, noticeAdapter)

        activity.onAttachFragment(this)
    }

    private fun initRecyclerView(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
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
