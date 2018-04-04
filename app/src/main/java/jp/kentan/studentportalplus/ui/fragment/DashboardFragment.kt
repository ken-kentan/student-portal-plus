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
import android.widget.TextView
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.ui.LectureCancellationActivity
import jp.kentan.studentportalplus.ui.LectureInformationActivity
import jp.kentan.studentportalplus.ui.MainActivity
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
        val activity = requireActivity() as MainActivity

        val viewModel = ViewModelProvider(activity, viewModelFactory).get(DashboardFragmentViewModel::class.java)

        val lectureInfoAdapter = LectureInformationAdapter(context, LectureInformationAdapter.TYPE_SMALL, object : LectureInformationAdapter.Listener{
            override fun onClick(data: LectureInformation) {
                viewModel.updateLectureInformation(data.copy(hasRead = true))
                startActivity<LectureInformationActivity>("id" to data.id)
            }
        }, MAX_LIST_SIZE)

        val lectureCancelAdapter =  LectureCancellationAdapter(context, LectureCancellationAdapter.TYPE_SMALL, object : LectureCancellationAdapter.Listener{
            override fun onClick(data: LectureCancellation) {
                viewModel.updateLectureCancellation(data.copy(hasRead = true))
                startActivity<LectureCancellationActivity>("id" to data.id)
            }
        }, MAX_LIST_SIZE)

        val noticeAdapter = NoticeAdapter(context, NoticeAdapter.TYPE_SMALL, object : NoticeAdapter.Listener{
            override fun onUpdateFavorite(data: Notice, isFavorite: Boolean) {
                viewModel.updateNotice(data.copy(isFavorite = isFavorite))
            }

            override fun onClick(data: Notice) {
                viewModel.updateNotice(data.copy(hasRead = true))
                startActivity<NoticeActivity>("id" to data.id)
            }
        }, MAX_LIST_SIZE)

        viewModel.getAttendLectureInformationList().observe(this, Observer {
            updateCardView(
                    lecture_info_header,
                    lecture_info_text,
                    lecture_info_button,
                    R.string.name_lecture_info,
                    it?.size ?: 0)

            lectureInfoAdapter.submitList(it)
            TransitionManager.beginDelayedTransition(dashboard_layout)
        })
        viewModel.getAttendLectureCancellationList().observe(this, Observer {
            updateCardView(
                    lecture_cancel_header,
                    lecture_cancel_text,
                    lecture_cancel_button,
                    R.string.name_lecture_cancel,
                    it?.size ?: 0)

            lectureCancelAdapter.submitList(it)
            TransitionManager.beginDelayedTransition(dashboard_layout)
        })
        viewModel.getNoticeList().observe(this, Observer {
            noticeAdapter.submitList(it)
            TransitionManager.beginDelayedTransition(dashboard_layout)
        })

        initRecyclerView(lecture_info_recycler_view, lectureInfoAdapter)
        initRecyclerView(lecture_cancel_recycler_view, lectureCancelAdapter)
        initRecyclerView(notice_recycler_view, noticeAdapter)

        lecture_info_text.text = getString(R.string.text_no_data, getString(R.string.name_lecture_info))
        lecture_cancel_text.text = getString(R.string.text_no_data, getString(R.string.name_lecture_cancel))

        lecture_info_button.setOnClickListener {
            activity.switchFragment(MainActivity.FragmentType.LECTURE_INFO)
        }
        lecture_cancel_button.setOnClickListener {
            activity.switchFragment(MainActivity.FragmentType.LECTURE_CANCEL)
        }
        notice_button.setOnClickListener {
            activity.switchFragment(MainActivity.FragmentType.NOTICE)
        }

        activity.onAttachFragment(this)
    }

    private fun initRecyclerView(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = adapter
        view.isNestedScrollingEnabled = false
        view.setHasFixedSize(false)
    }

    private fun updateCardView(header: TextView, text: TextView, button: TextView, titleId: Int, itemCount: Int) {
        header.text = getString(titleId)

        if (itemCount <= 0) {
            text.visibility   = View.VISIBLE
            button.visibility = View.GONE
        } else if (itemCount > MAX_LIST_SIZE) {
            header.append(getString(R.string.text_more_item, itemCount - MAX_LIST_SIZE))

            text.visibility   = View.GONE
            button.visibility = View.VISIBLE
        } else {
            text.visibility   = View.GONE
            button.visibility = View.GONE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = DashboardFragment()

        private const val MAX_LIST_SIZE = 3
    }
}
