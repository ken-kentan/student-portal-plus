package jp.kentan.studentportalplus.ui.lectures.cancellation

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import dagger.android.support.DaggerFragment
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.databinding.FragmentListBinding
import jp.kentan.studentportalplus.ui.lecturecancellationdetail.LectureCancellationDetailActivity
import jp.kentan.studentportalplus.ui.lectures.LecturesAdapter
import jp.kentan.studentportalplus.ui.lectures.LecturesFilterDialogFragment
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.view.widget.DividerItemDecoration
import javax.inject.Inject

class LectureCancellationsFragment : DaggerFragment(R.layout.fragment_list),
    LecturesFilterDialogFragment.Listener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val lectureCancelsViewModel by activityViewModels<LectureCancellationsViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        val lecturesAdapter = LecturesAdapter(
            R.string.lecture_cancellations_not_found,
            lectureCancelsViewModel.onItemClick
        )

        FragmentListBinding.bind(view).recyclerView.apply {
            adapter = lecturesAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext()))
        }

        lectureCancelsViewModel.lectureCancelList.observe(
            viewLifecycleOwner,
            lecturesAdapter::submitList
        )

        lectureCancelsViewModel.startDetailActivity.observeEvent(viewLifecycleOwner) {
            startActivity(LectureCancellationDetailActivity.createIntent(requireContext(), it))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.query, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            queryHint = getString(R.string.all_subject_instructor_query)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = true

                override fun onQueryTextChange(newText: String?): Boolean {
                    lectureCancelsViewModel.onQueryTextChange(newText)
                    return true
                }
            })
        }

        val queryText = lectureCancelsViewModel.queryText
        if (!queryText.isNullOrBlank()) {
            searchItem.expandActionView()
            searchView.setQuery(queryText, false)
            searchView.clearFocus()
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filter) {
            LecturesFilterDialogFragment.newInstance(
                lectureCancelsViewModel.query
            ).show(childFragmentManager, "lecture_cancels_filter")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFilterApplyClick(lectureQuery: LectureQuery) {
        lectureCancelsViewModel.onFilterApplyClick(lectureQuery)
    }
}
