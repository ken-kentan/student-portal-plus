package jp.kentan.studentportalplus.ui.notice

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.NoticeQuery
import jp.kentan.studentportalplus.databinding.DialogNoticeFilterBinding
import jp.kentan.studentportalplus.databinding.FragmentListBinding
import jp.kentan.studentportalplus.ui.ViewModelFactory
import jp.kentan.studentportalplus.ui.main.FragmentType
import jp.kentan.studentportalplus.ui.main.MainViewModel
import jp.kentan.studentportalplus.ui.notice.detail.NoticeDetailActivity
import jp.kentan.studentportalplus.util.animateFadeInDelay
import javax.inject.Inject

class NoticeFragment : Fragment() {

    companion object {
        fun newInstance() = NoticeFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentListBinding
    private lateinit var viewModel: NoticeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        setHasOptionsMenu(true)

        val provider = ViewModelProvider(requireActivity(), viewModelFactory)

        viewModel = provider.get(NoticeViewModel::class.java)

        val adapter = NoticeAdapter(layoutInflater, viewModel::onClick, viewModel::onFavoriteClick)

        binding.recyclerView.apply {
            setAdapter(adapter)
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        binding.note.text = getString(R.string.text_empty_data, getString(R.string.name_notice))

        viewModel.subscribe(adapter)

        // Call MainViewModel::onAttachFragment
        provider.get(MainViewModel::class.java)
                .onAttachFragment(FragmentType.NOTICE)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_and_filter, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.hint_query_title)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onQueryTextChange(newText)
                return true
            }
        })

        val keyword = viewModel.query.keyword
        if (!keyword.isNullOrBlank()) {
            searchItem.expandActionView()
            searchView.setQuery(keyword, false)
            searchView.clearFocus()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filter) {
            showFilterDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFilterDialog() {
        val context = requireContext()

        val binding: DialogNoticeFilterBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.dialog_notice_filter, binding.root as ViewGroup, false)

        binding.apply {
            dateRangeSpinner.adapter = ArrayAdapter<NoticeQuery.DateRange>(context, android.R.layout.simple_list_item_1, NoticeQuery.DateRange.values())
            query = this@NoticeFragment.viewModel.query
        }

        AlertDialog.Builder(context)
                .setView(binding.root)
                .setTitle(R.string.title_filter_dialog)
                .setPositiveButton(R.string.action_apply) { _, _ ->
                    viewModel.onFilterApplyClick(
                            binding.dateRangeSpinner.selectedItem as NoticeQuery.DateRange,
                            binding.unreadChip.isChecked,
                            binding.readChip.isChecked,
                            binding.favoriteChip.isChecked
                    )
                }
                .setNegativeButton(R.string.action_cancel, null)
                .create()
                .show()
    }

    private fun NoticeViewModel.subscribe(adapter: NoticeAdapter) {
        val fragment = this@NoticeFragment

        noticeList.observe(fragment, Observer { list ->
            adapter.submitList(list)

            if (list.isEmpty()) {
                binding.note.animateFadeInDelay(requireContext())
            } else {
                binding.note.apply {
                    alpha = 0f
                    isVisible = false
                }
            }
        })
        startDetailActivity.observe(fragment, Observer { id ->
            startActivity(NoticeDetailActivity.createIntent(requireContext(), id))
        })
    }
}
