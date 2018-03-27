package jp.kentan.studentportalplus.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import dagger.android.support.AndroidSupportInjection

import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.ui.LectureInformationActivity
import jp.kentan.studentportalplus.ui.adapter.LectureInformationAdapter
import jp.kentan.studentportalplus.ui.viewmodel.LectureInformationFragmentViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.animateFadeInDelay
import kotlinx.android.synthetic.main.fragment_lecture_information.*
import org.jetbrains.anko.support.v4.startActivity
import javax.inject.Inject


class LectureInformationFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: LectureInformationFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lecture_information, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        setHasOptionsMenu(true)

        val context  = requireContext()
        val activity = requireActivity()

        viewModel = ViewModelProvider(this, viewModelFactory).get(LectureInformationFragmentViewModel::class.java)

        val adapter = LectureInformationAdapter(context, LectureInformationAdapter.TYPE_NORMAL, object : LectureInformationAdapter.Listener{
            override fun onClick(data: LectureInformation) {
                viewModel.update(data.copy(hasRead = true))
                startActivity<LectureInformationActivity>("id" to data.id, "title" to data.subject)
            }
        })

        viewModel.getResults().observe(this, Observer {
            adapter.submitList(it)

            if (it == null || it.isEmpty()) {
                text.animateFadeInDelay(context)
            } else {
                text.alpha = 0f
                text.visibility = View.GONE
            }
        })

        text.text = getString(R.string.msg_not_found, getString(R.string.name_lecture_info))

        initRecyclerView(recycler_view, adapter)

        activity.onAttachFragment(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_and_filter, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = adapter
        view.setHasFixedSize(true)
    }

    companion object {
        var instance: LectureInformationFragment? = null
            private set
            get() {
                if (field == null) {
                    field = LectureInformationFragment()
                }

                return field
            }
    }
}
