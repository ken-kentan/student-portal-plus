package jp.kentan.studentportalplus.ui.lectures.information

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.databinding.FragmentListBinding
import jp.kentan.studentportalplus.ui.lectureinformationdetail.LectureInformationDetailActivity
import jp.kentan.studentportalplus.ui.lectures.LecturesAdapter
import jp.kentan.studentportalplus.ui.lectures.LecturesFilterDialogFragment
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.view.widget.DividerItemDecoration
import javax.inject.Inject

class LectureInformationsFragment : Fragment(R.layout.fragment_list),
    LecturesFilterDialogFragment.Listener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val lectureInfosViewModel by activityViewModels<LectureInformationsViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        val lecturesAdapter = LecturesAdapter(
            R.string.text_empty_lecture_information,
            lectureInfosViewModel.onItemClick
        )

        FragmentListBinding.bind(view).recyclerView.apply {
            adapter = lecturesAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext()))
        }

        lectureInfosViewModel.lectureInfoList.observe(
            viewLifecycleOwner,
            lecturesAdapter::submitList
        )
        lectureInfosViewModel.startDetailActivity.observeEvent(viewLifecycleOwner) {
            startActivity(LectureInformationDetailActivity.createIntent(requireContext(), it))
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.query, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            queryHint = getString(R.string.hint_query_subject_instructor)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = true

                override fun onQueryTextChange(newText: String?): Boolean {
                    lectureInfosViewModel.onQueryTextChange(newText)
                    return true
                }
            })
        }

        val queryText = lectureInfosViewModel.queryText
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
                lectureInfosViewModel.query
            ).show(childFragmentManager, "lecture_infos_filter")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFilterApplyClick(lectureQuery: LectureQuery) {
        lectureInfosViewModel.onFilterApplyClick(lectureQuery)
    }
}
