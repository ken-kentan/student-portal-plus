package jp.kentan.studentportalplus.ui.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection

import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.ui.MyClassActivity
import jp.kentan.studentportalplus.ui.MyClassEditActivity
import jp.kentan.studentportalplus.ui.adapter.MyClassAdapter
import jp.kentan.studentportalplus.ui.viewmodel.TimetableFragmentViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_timetable.*
import org.jetbrains.anko.support.v4.startActivity
import javax.inject.Inject

class TimetableFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: TimetableFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timetable, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val context  = requireContext()
        val activity = requireActivity()

        viewModel = ViewModelProvider(activity, viewModelFactory).get(TimetableFragmentViewModel::class.java)

        val adapter = MyClassAdapter(context, MyClassAdapter.TYPE_GRID, object : MyClassAdapter.Listener{
            override fun onClick(data: MyClass) {
                startActivity<MyClassActivity>("id" to data.id)
            }
            override fun onAddClick(period: Int, week: ClassWeekType) {
                startActivity<MyClassEditActivity>("period" to period, "week" to week)
            }
        })

        viewModel.getResults().observe(this, Observer {
            adapter.submitList(it)
            TransitionManager.beginDelayedTransition(grid_recycler_view)
        })

        // Initialize RecyclerViews
        grid_recycler_view.layoutManager = GridLayoutManager(context, 5)
        grid_recycler_view.adapter = adapter
        grid_recycler_view.isNestedScrollingEnabled = false
        grid_recycler_view.setHasFixedSize(true)

        activity.onAttachFragment(this)
    }

    companion object {
        @JvmStatic
        fun newInstance() = TimetableFragment()
    }
}
