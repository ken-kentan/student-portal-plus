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
import jp.kentan.studentportalplus.ui.*
import jp.kentan.studentportalplus.ui.adapter.DashboardLectureCancellationAdapter
import jp.kentan.studentportalplus.ui.adapter.DashboardLectureInformationAdapter
import jp.kentan.studentportalplus.ui.adapter.DashboardMyClassAdapter
import jp.kentan.studentportalplus.ui.adapter.DashboardNoticeAdapter
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

        val myClassAdapter = DashboardMyClassAdapter(context) {
            val intent = MyClassActivity.createIntent(requireContext(), it.id)
            startActivity(intent)
        }

        val lectureInfoAdapter = DashboardLectureInformationAdapter(context , MAX_LIST_SIZE) {
            startActivity<LectureInformationActivity>("id" to it.id)
        }

        val lectureCancelAdapter = DashboardLectureCancellationAdapter(context, MAX_LIST_SIZE) {
            startActivity<LectureCancellationActivity>("id" to it.id)
        }

        val noticeAdapter = DashboardNoticeAdapter(context, MAX_LIST_SIZE, onClick = {
            startActivity<NoticeActivity>("id" to it.id)
        }, onClickFavorite = {
            viewModel.onClickNoticeFavorite(it)
        })

        viewModel.portalDataSet.observe(this, Observer { set ->
            if (set == null) {
                return@Observer
            }

            TransitionManager.beginDelayedTransition(dashboard_layout)

            if (set.myClassList.isNotEmpty()) {
                timetable_card_view.visibility = View.VISIBLE
                timetable_recycler_view.visibility = View.VISIBLE
                timetable_header.text = getString(R.string.name_timetable, set.myClassList.first().week.fullDisplayName)
            } else {
                timetable_card_view.visibility = View.GONE
                timetable_recycler_view.visibility = View.GONE
            }
            myClassAdapter.submitList(set.myClassList)

            updateCardView(
                    lecture_info_header,
                    lecture_info_note,
                    lecture_info_button,
                    R.string.name_lecture_info,
                    set.lectureInfoList.size)

            lectureInfoAdapter.submitList(set.lectureInfoList)

            updateCardView(
                    lecture_cancel_header,
                    lecture_cancel_note,
                    lecture_cancel_button,
                    R.string.name_lecture_cancel,
                    set.lectureCancelList.size)

            lectureCancelAdapter.submitList(set.lectureCancelList)

            noticeAdapter.submitList(set.noticeList)
        })

        initRecyclerView(timetable_recycler_view, myClassAdapter)
        initRecyclerView(lecture_info_recycler_view, lectureInfoAdapter)
        initRecyclerView(lecture_cancel_recycler_view, lectureCancelAdapter)
        initRecyclerView(notice_recycler_view, noticeAdapter)

        lecture_info_note.text = getString(R.string.text_no_data, getString(R.string.name_lecture_info))
        lecture_cancel_note.text = getString(R.string.text_no_data, getString(R.string.name_lecture_cancel))

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

        when {
            itemCount <= 0 -> {
                text.visibility   = View.VISIBLE
                button.visibility = View.GONE
            }
            itemCount > MAX_LIST_SIZE -> {
                header.append(getString(R.string.text_more_item, itemCount - MAX_LIST_SIZE))

                text.visibility   = View.GONE
                button.visibility = View.VISIBLE
            }
            else -> {
                text.visibility   = View.GONE
                button.visibility = View.GONE
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = DashboardFragment()

        private const val MAX_LIST_SIZE = 3
    }
}
