package jp.kentan.studentportalplus.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import dagger.android.support.DaggerFragment
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.FragmentDashboardBinding
import jp.kentan.studentportalplus.ui.dashboard.adapter.LectureCancellationAdapter
import jp.kentan.studentportalplus.ui.dashboard.adapter.LectureInformationAdapter
import jp.kentan.studentportalplus.ui.dashboard.adapter.NoticeAdapter
import jp.kentan.studentportalplus.ui.dashboard.adapter.TimetableAdapter
import jp.kentan.studentportalplus.ui.lecturecancellationdetail.LectureCancellationDetailActivity
import jp.kentan.studentportalplus.ui.lectureinformationdetail.LectureInformationDetailActivity
import jp.kentan.studentportalplus.ui.mycoursedetail.MyCourseDetailActivity
import jp.kentan.studentportalplus.ui.noticedetail.NoticeDetailActivity
import jp.kentan.studentportalplus.ui.observeEvent
import javax.inject.Inject

class DashboardFragment : DaggerFragment(R.layout.fragment_dashboard) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val dashboardViewModel by activityViewModels<DashboardViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentDashboardBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = dashboardViewModel
        }

        val timetableAdapter = TimetableAdapter(
            parentCardView = binding.timetableCardView,
            onItemClick = dashboardViewModel.onMyCourseItemClick
        )
        val lectureInfoAdapter = LectureInformationAdapter(
            onItemClick = dashboardViewModel.onLectureInformationItemClick,
            onShowAllClick = dashboardViewModel.onLectureInformationShowAllClick
        )
        val lectureCancelAdapter = LectureCancellationAdapter(
            onItemClick = dashboardViewModel.onLectureCancellationItemClick,
            onShowAllClick = dashboardViewModel.onLectureCancellationShowAllClick
        )
        val noticeAdapter = NoticeAdapter(
            onItemClick = dashboardViewModel.onNoticeItemClick,
            onFavoriteClick = dashboardViewModel.onNoticeFavoriteClick,
            onShowAllClick = dashboardViewModel.onNoticeShowAllClick
        )

        binding.apply {
            timetableRecyclerView.setup(timetableAdapter)
            lectureInformationRecyclerView.setup(lectureInfoAdapter)
            lectureCancellationRecyclerView.setup(lectureCancelAdapter)
            noticeRecyclerView.setup(noticeAdapter)
        }

        dashboardViewModel.portalSet.observe(viewLifecycleOwner) { set ->
            TransitionManager.beginDelayedTransition(binding.layout)

            timetableAdapter.submitList(set.myCourseList)
            lectureInfoAdapter.submitList(set.lectureInfoList)
            lectureCancelAdapter.submitList(set.lectureCancelList)
            noticeAdapter.submitList(set.noticeList)

            binding.layout.requestLayout()
        }

        val navOptions = navOptions {
            anim {
                enter = R.anim.nav_default_enter_anim
                exit = R.anim.nav_default_exit_anim
                popEnter = R.anim.nav_default_pop_enter_anim
                popExit = R.anim.nav_default_pop_exit_anim
            }
        }

        dashboardViewModel.navigate.observeEvent(viewLifecycleOwner) {
            findNavController().navigate(it, null, navOptions)
        }
        dashboardViewModel.startMyCourseDetailActivity.observeEvent(viewLifecycleOwner) {
            startActivity(MyCourseDetailActivity.createIntent(requireContext(), it))
        }
        dashboardViewModel.startLectureInfoActivity.observeEvent(viewLifecycleOwner) {
            startActivity(LectureInformationDetailActivity.createIntent(requireContext(), it))
        }
        dashboardViewModel.startLectureCancelActivity.observeEvent(viewLifecycleOwner) {
            startActivity(LectureCancellationDetailActivity.createIntent(requireContext(), it))
        }
        dashboardViewModel.startNoticeActivity.observeEvent(viewLifecycleOwner) {
            startActivity(NoticeDetailActivity.createIntent(requireContext(), it))
        }
    }

    private fun RecyclerView.setup(adapter: RecyclerView.Adapter<*>) {
        isNestedScrollingEnabled = false
        setAdapter(adapter)
        setHasFixedSize(false)
        itemAnimator = null
    }
}
