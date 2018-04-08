package jp.kentan.studentportalplus.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.AsyncLayoutInflater
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import dagger.android.support.AndroidSupportInjection

import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.LectureOrderType
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.ui.LectureInformationActivity
import jp.kentan.studentportalplus.ui.adapter.LectureInformationAdapter
import jp.kentan.studentportalplus.ui.viewmodel.LectureInformationFragmentViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.animateFadeInDelay
import kotlinx.android.synthetic.main.dialog_lecture_filter.view.*
import kotlinx.android.synthetic.main.fragment_lecture.*
import org.jetbrains.anko.support.v4.startActivity
import javax.inject.Inject


class LectureInformationFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: LectureInformationFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lecture, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        setHasOptionsMenu(true)

        val context  = requireContext()
        val activity = requireActivity()

        viewModel = ViewModelProvider(activity, viewModelFactory).get(LectureInformationFragmentViewModel::class.java)

        val adapter = LectureInformationAdapter(context, LectureInformationAdapter.TYPE_NORMAL, object : LectureInformationAdapter.Listener{
            override fun onClick(data: LectureInformation) {
                viewModel.update(data.copy(hasRead = true))
                startActivity<LectureInformationActivity>("id" to data.id)
            }
        })

        viewModel.getResults().observe(this, Observer {
            adapter.submitList(it)

            if (it == null || it.isEmpty()) {
                note.animateFadeInDelay(context)
            } else {
                note.alpha = 0f
                note.visibility = View.GONE
            }
        })

        note.text = getString(R.string.msg_not_found, getString(R.string.name_lecture_info))

        initRecyclerView(recycler_view, adapter)

        activity.onAttachFragment(this)
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
                viewModel.query = viewModel.query.copy(keywords = newText)
                return true
            }
        })

        val keywords = viewModel.query.keywords
        if (!keywords.isNullOrBlank()) {
            searchItem.expandActionView()
            searchView.setQuery(keywords, false)
            searchView.clearFocus()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_list_filter) {
            showFilterDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = adapter
        view.setHasFixedSize(true)
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val context = requireContext()

        val changeListener = CompoundButton.OnCheckedChangeListener{ button, checked ->
            val color = ContextCompat.getColor(context, if (checked) R.color.chip_checked_text else R.color.chip_unchecked_text)
            button.setTextColor(color)
        }

        AsyncLayoutInflater(context).inflate(R.layout.dialog_lecture_filter, null) { view, _, _ ->
            view.unread_check_box.setOnCheckedChangeListener(changeListener)
            view.read_check_box.setOnCheckedChangeListener(changeListener)
            view.attend_check_box.setOnCheckedChangeListener(changeListener)

            view.order_spinner.adapter =
                    ArrayAdapter<LectureOrderType>(requireContext(), android.R.layout.simple_list_item_1, LectureOrderType.values())

            val query = viewModel.query
            view.order_spinner.setSelection(query.order.ordinal)
            view.unread_check_box.isChecked = query.isUnread
            view.read_check_box.isChecked   = query.hasRead
            view.attend_check_box.isChecked = query.isAttend

            AlertDialog.Builder(context)
                    .setView(view)
                    .setTitle(R.string.title_filter_dialog)
                    .setPositiveButton(R.string.action_apply) { _, _ ->
                        viewModel.query = viewModel.query.copy(
                                order    = view.order_spinner.selectedItem as LectureOrderType,
                                isUnread = view.unread_check_box.isChecked,
                                hasRead  = view.read_check_box.isChecked,
                                isAttend = view.attend_check_box.isChecked
                        )
                    }
                    .setNegativeButton(R.string.action_cancel, null)
                    .create()
                    .show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = LectureInformationFragment()
    }
}
