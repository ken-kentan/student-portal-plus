package jp.kentan.studentportalplus.ui.notices

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
import jp.kentan.studentportalplus.data.vo.NoticeQuery
import jp.kentan.studentportalplus.databinding.FragmentListBinding
import jp.kentan.studentportalplus.ui.noticedetail.NoticeDetailActivity
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.view.widget.DividerItemDecoration
import javax.inject.Inject

class NoticesFragment : Fragment(R.layout.fragment_list), NoticesFilterDialogFragment.Listener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val noticeViewModel by activityViewModels<NoticesViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        val noticeAdapter = NoticesAdapter(noticeViewModel.onItemClick)

        FragmentListBinding.bind(view).recyclerView.apply {
            adapter = noticeAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext()))
        }

        noticeViewModel.lectureInfoList.observe(viewLifecycleOwner, noticeAdapter::submitList)
        noticeViewModel.startDetailActivity.observeEvent(viewLifecycleOwner) {
            startActivity(NoticeDetailActivity.createIntent(requireContext(), it))
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
                    noticeViewModel.onQueryTextChange(newText)
                    return true
                }
            })
        }

        val queryText = noticeViewModel.queryText
        if (!queryText.isNullOrBlank()) {
            searchItem.expandActionView()
            searchView.setQuery(queryText, false)
            searchView.clearFocus()
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filter) {
            NoticesFilterDialogFragment.newInstance(
                noticeViewModel.query
            ).show(childFragmentManager, "notices_filter")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFilterApplyClick(noticeQuery: NoticeQuery) {
        noticeViewModel.onFilterApplyClick(noticeQuery)
    }
}
