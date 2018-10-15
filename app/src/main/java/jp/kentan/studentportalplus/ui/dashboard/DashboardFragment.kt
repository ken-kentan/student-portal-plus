package jp.kentan.studentportalplus.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.Lecture
import jp.kentan.studentportalplus.databinding.FragmentDashboardBinding
import jp.kentan.studentportalplus.ui.ViewModelFactory
import jp.kentan.studentportalplus.ui.lecturecancel.detail.LectureCancelDetailActivity
import jp.kentan.studentportalplus.ui.lectureinfo.detail.LectureInfoDetailActivity
import jp.kentan.studentportalplus.ui.main.FragmentType
import jp.kentan.studentportalplus.ui.main.MainViewModel
import jp.kentan.studentportalplus.ui.myclass.detail.MyClassDetailActivity
import jp.kentan.studentportalplus.ui.notice.detail.NoticeDetailActivity
import javax.inject.Inject

class DashboardFragment : Fragment() {

    companion object {
        private val TRANSITION by lazy(LazyThreadSafetyMode.NONE) {
            TransitionSet()
                    .setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
                    .addTransition(ChangeBounds())
                    .addTransition(Fade(Fade.IN))
        }

        fun newInstance() = DashboardFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentDashboardBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val provider = ViewModelProvider(requireActivity(), viewModelFactory)

        val viewModel = provider.get(DashboardViewModel::class.java)
        val mainViewModel = provider.get(MainViewModel::class.java)

        binding.viewModel = viewModel
        binding.mainViewModel = mainViewModel

        val myClassAdapter = MyClassAdapter(layoutInflater, viewModel::onMyClassClick)
        val lectureInfoAdapter = LectureAdapter(layoutInflater, viewModel::onLectureInfoClick)
        val lectureCancelAdapter = LectureAdapter(layoutInflater, viewModel::onLectureCancelClick)
        val noticeAdapter = NoticeAdapter(layoutInflater, viewModel::onNoticeItemClick, viewModel::onNoticeFavoriteClick)

        binding.apply {
            timetableRecyclerView.setup(myClassAdapter)
            lectureInfoRecyclerView.setup(lectureInfoAdapter)
            lectureCancelRecyclerView.setup(lectureCancelAdapter)
            noticeRecyclerView.setup(noticeAdapter)
        }

        viewModel.subscribe(myClassAdapter, lectureInfoAdapter, lectureCancelAdapter, noticeAdapter)

        // Call MainViewModel::onAttachFragment
        mainViewModel.onAttachFragment(FragmentType.DASHBOARD)
    }

    private fun DashboardViewModel.subscribe(
            myClassAdapter: MyClassAdapter,
            lectureInfoAdapter: LectureAdapter,
            lectureCancelAdapter: LectureAdapter,
            noticeAdapter: NoticeAdapter
    ) {
        val fragment = this@DashboardFragment

        portalDataSet.observe(fragment, Observer { set ->
            TransitionManager.beginDelayedTransition(binding.layout, TRANSITION)

            myClassAdapter.submitList(set.myClassList)
            lectureInfoAdapter.submitList(set.lectureInfoList.take(DashboardViewModel.MAX_ITEM_SIZE), set.lectureInfoList.isInvisibleLastDivider())
            lectureCancelAdapter.submitList(set.lectureCancelList.take(DashboardViewModel.MAX_ITEM_SIZE), set.lectureCancelList.isInvisibleLastDivider())
            noticeAdapter.submitList(set.noticeList)

            if (set.myClassList.isNotEmpty()) {
                val week = set.myClassList.first().week
                binding.timetableHeader.text = getString(R.string.name_timetable, week.fullDisplayName)
                binding.timetableCard.isVisible = true
            } else {
                binding.timetableCard.isVisible = false
            }

            updateCardView(
                    binding.lectureInfoHeader,
                    binding.lectureInfoNote,
                    binding.lectureInfoButton,
                    R.string.name_lecture_info,
                    set.lectureInfoList.size)

            updateCardView(
                    binding.lectureCancelHeader,
                    binding.lectureCancelNote,
                    binding.lectureCancelButton,
                    R.string.name_lecture_cancel,
                    set.lectureCancelList.size)
        })

        startMyClassDetailActivity.observe(fragment, Observer { id ->
            startActivity(MyClassDetailActivity.createIntent(requireContext(), id))
        })

        startLectureInfoActivity.observe(fragment, Observer { id ->
            startActivity(LectureInfoDetailActivity.createIntent(requireContext(), id))
        })
        startLectureCancelActivity.observe(fragment, Observer { id ->
            startActivity(LectureCancelDetailActivity.createIntent(requireContext(), id))
        })
        startNoticeDetailActivity.observe(fragment, Observer { id ->
            startActivity(NoticeDetailActivity.createIntent(requireContext(), id))
        })
    }

    private fun RecyclerView.setup(adapter: RecyclerView.Adapter<*>) {
        isNestedScrollingEnabled = false
        setAdapter(adapter)
        setHasFixedSize(false)
    }

    private fun updateCardView(header: TextView, note: TextView, button: TextView, titleId: Int, itemCount: Int) {
        header.text = getString(titleId)

        when {
            itemCount <= 0 -> {
                note.isVisible = true
                button.isVisible = false
            }
            itemCount > DashboardViewModel.MAX_ITEM_SIZE -> {
                header.append(getString(R.string.text_more_item, itemCount - DashboardViewModel.MAX_ITEM_SIZE))

                note.isVisible = false
                button.isVisible = true
            }
            else -> { // 1-3
                button.isVisible = false
                note.isVisible = false
            }
        }
    }

    private fun List<Lecture>.isInvisibleLastDivider() = size <= DashboardViewModel.MAX_ITEM_SIZE
}
