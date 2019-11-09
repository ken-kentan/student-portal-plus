package jp.kentan.studentportalplus.ui.lectureinformation

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
import jp.kentan.studentportalplus.databinding.FragmentListBinding
import jp.kentan.studentportalplus.ui.LectureFilterDialogFragment
import jp.kentan.studentportalplus.ui.lectureinformationdetail.LectureInformationDetailActivity
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.view.widget.DividerItemDecoration
import javax.inject.Inject

class LectureInformationFragment : Fragment(R.layout.fragment_list) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val lectureInfoViewModel by activityViewModels<LectureInformationViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lectureInfoAdapter = LectureInformationAdapter(lectureInfoViewModel.onItemClick)

        FragmentListBinding.bind(view).recyclerView.apply {
            adapter = lectureInfoAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext()))
        }

        lectureInfoViewModel.lectureInfoList.observe(
            viewLifecycleOwner,
            lectureInfoAdapter::submitList
        )
        lectureInfoViewModel.startDetailActivity.observeEvent(viewLifecycleOwner) {
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
                    if (searchItem.isActionViewExpanded) {
                        lectureInfoViewModel.onQueryTextChange(newText)
                    }
                    return true
                }
            })
        }

        val queryText = lectureInfoViewModel.queryText
        if (!queryText.isNullOrBlank()) {
            searchItem.expandActionView()
            searchView.setQuery(queryText, false)
            searchView.clearFocus()
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filter) {
            LectureFilterDialogFragment(
                lectureInfoViewModel.query,
                lectureInfoViewModel.onFilterApplyClick
            ).show(parentFragmentManager, "lecture_info_query")
        }
        return super.onOptionsItemSelected(item)
    }
}
