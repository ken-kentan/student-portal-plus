package jp.kentan.studentportalplus.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.Notice
import jp.kentan.studentportalplus.ui.NoticeActivity
import jp.kentan.studentportalplus.ui.adapter.NoticeAdapter
import jp.kentan.studentportalplus.ui.viewmodel.NoticeFragmentViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.AnimationHelper
import kotlinx.android.synthetic.main.fragment_notice.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject


class NoticeFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: NoticeFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notice, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        setHasOptionsMenu(true)

        val context  = requireContext()
        val activity = requireActivity()

        viewModel = ViewModelProvider(activity, viewModelFactory).get(NoticeFragmentViewModel::class.java)

        val adapter = NoticeAdapter(context, NoticeAdapter.TYPE_NORMAL, object : NoticeAdapter.Listener{
            override fun onUpdateFavorite(data: Notice, isFavorite: Boolean) { }

            override fun onClick(data: Notice) {
                viewModel.updateNotice(data.copy(hasRead = true))
                startActivity<NoticeActivity>("id" to data.id, "title" to data.title)
            }
        })

        viewModel.getNotices().observe(this, Observer {
            adapter.submitList(it)

            if (it == null || it.isEmpty()) {
                if (text.visibility == View.GONE) {
                    text.startAnimation(AnimationHelper.fadeIn(text))
                }
            } else {
                if (text.visibility == View.VISIBLE) {
                    text.startAnimation(AnimationHelper.fadeOut(text))
                }
            }
        })

        text.text = getString(R.string.msg_not_found, getString(R.string.name_notice))

        initRecyclerView(recycler_view, adapter)

        activity.onAttachFragment(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_and_filter, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.hint_query_title)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.query = newText
                return true
            }
        })

        val query = viewModel.query
        if (!query.isNullOrBlank()) {
            searchItem.expandActionView()
            searchView.setQuery(query, false)
            searchView.clearFocus()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_list_filter) {
            toast("action_list_filter")
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = adapter
        view.setHasFixedSize(true)
    }

    companion object {
        var instance: NoticeFragment? = null
            private set
            get() {
                if (field == null) {
                    field = NoticeFragment()
                }

                return field
            }
    }
}
