package jp.kentan.studentportalplus.ui.lectureinfo

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.databinding.DialogLectureFilterBinding
import jp.kentan.studentportalplus.databinding.FragmentListBinding
import jp.kentan.studentportalplus.ui.ViewModelFactory
import jp.kentan.studentportalplus.ui.lectureinfo.detail.LectureInfoDetailActivity
import jp.kentan.studentportalplus.ui.main.FragmentType
import jp.kentan.studentportalplus.ui.main.MainViewModel
import javax.inject.Inject

class LectureInfoFragment : Fragment() {

    companion object {
        fun newInstance() = LectureInfoFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentListBinding
    private lateinit var viewModel: LectureInfoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        setHasOptionsMenu(true)

        val provider = ViewModelProvider(requireActivity(), viewModelFactory)

        viewModel = provider.get(LectureInfoViewModel::class.java)

        val adapter = LectureInfoAdapter(layoutInflater, viewModel::onClick)

        binding.recyclerView.apply {
            setAdapter(adapter)
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        viewModel.subscribe(adapter)

        // Call MainViewModel::onAttachFragment
        provider.get(MainViewModel::class.java)
                .onAttachFragment(FragmentType.LECTURE_INFO)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_and_filter, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.hint_query_subject_and_instructor)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_filter) {
            showFilterDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFilterDialog() {
        val context = requireContext()

        val binding: DialogLectureFilterBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.dialog_lecture_filter, binding.root as ViewGroup, false)

        binding.apply {
            orderSpinner.adapter = ArrayAdapter<LectureQuery.Order>(context, android.R.layout.simple_list_item_1, LectureQuery.Order.values())
            query = this@LectureInfoFragment.viewModel.query
        }

        AlertDialog.Builder(context)
                .setView(binding.root)
                .setTitle(R.string.title_filter_dialog)
                .setPositiveButton(R.string.action_apply) { _, _ ->
                    viewModel.onFilterApplyClick(
                            binding.orderSpinner.selectedItem as LectureQuery.Order,
                            binding.unreadChip.isChecked,
                            binding.readChip.isChecked,
                            binding.attendChip.isChecked
                    )
                }
                .setNegativeButton(R.string.action_cancel, null)
                .create()
                .show()
    }

    private fun LectureInfoViewModel.subscribe(adapter: LectureInfoAdapter) {
        val fragment = this@LectureInfoFragment

        lectureInfoList.observe(fragment, Observer { adapter.submitList(it) })
        startDetailActivity.observe(fragment, Observer { id ->
            startActivity(LectureInfoDetailActivity.createIntent(requireContext(), id))
        })
    }
}
