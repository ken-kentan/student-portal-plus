package jp.kentan.studentportalplus.ui.timetable

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import dagger.android.support.DaggerFragment
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.FragmentTimetableBinding
import jp.kentan.studentportalplus.ui.editmycourse.EditMyCourseActivity
import jp.kentan.studentportalplus.ui.mycoursedetail.MyCourseDetailActivity
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.view.widget.DividerItemDecoration
import javax.inject.Inject

class TimetableFragment : DaggerFragment(R.layout.fragment_timetable) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val timetableViewModel by activityViewModels<TimetableViewModel> { viewModelFactory }

    private lateinit var timetableItemDecoration: TimetableItemDecoration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val gridTimetableAdapter = TimetableAdapter(
            layout = TimetableAdapter.Layout.GRID,
            onMyCourseClick = timetableViewModel.onMyCourseClick,
            onBlankClick = timetableViewModel.onBlankClick
        )
        val listTimetableAdapter = TimetableAdapter(
            layout = TimetableAdapter.Layout.LIST,
            onMyCourseClick = timetableViewModel.onMyCourseClick
        )

        timetableItemDecoration = TimetableItemDecoration(requireContext())

        val binding = FragmentTimetableBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = timetableViewModel
        }

        binding.gridRecyclerView.apply {
            adapter = gridTimetableAdapter
            setHasFixedSize(true)
            addItemDecoration(timetableItemDecoration)
            layoutManager = TimetableLayoutManager()
        }
        binding.listRecyclerView.apply {
            adapter = listTimetableAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext()))
        }

        timetableViewModel.myCourseList.observe(viewLifecycleOwner) {
            gridTimetableAdapter.submitList(it)
            listTimetableAdapter.submitList(it)
        }
        timetableViewModel.isGridLayout.observe(viewLifecycleOwner) { isGrid ->
            binding.viewSwitcher.displayedChild = if (isGrid) 0 else 1
        }
        timetableViewModel.startEditMyCourseActivity.observeEvent(viewLifecycleOwner) { (period, dayOfWeek) ->
            startActivity(
                EditMyCourseActivity.createIntent(
                    requireContext(),
                    period,
                    dayOfWeek
                )
            )
        }
        timetableViewModel.startMyCourseDetailActivity.observeEvent(viewLifecycleOwner) {
            startActivity(MyCourseDetailActivity.createIntent(requireContext(), it))
        }
    }

    override fun onResume() {
        super.onResume()
        timetableItemDecoration.syncCalenderByCurrentTime()
        timetableViewModel.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.timetable, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> timetableViewModel.onAddClick()
            R.id.action_switch_layout -> timetableViewModel.onSwitchLayoutClick()
        }
        return super.onOptionsItemSelected(item)
    }
}
