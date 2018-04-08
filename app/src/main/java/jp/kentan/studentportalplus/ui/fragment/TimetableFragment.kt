package jp.kentan.studentportalplus.ui.fragment

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.*
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
import org.jetbrains.anko.find
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

        setHasOptionsMenu(true)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.switch_layout, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_switch_layout) {
            showLayoutSelectPopup()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("RestrictedApi")
    private fun showLayoutSelectPopup() {
        val context = requireContext()
        val anchor: View = requireActivity().find(R.id.action_switch_layout)

        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.popup_switch_layout, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_view_week -> {}
                R.id.action_view_day  -> {}
            }
            return@setOnMenuItemClickListener true
        }
        popup.show()

        (popup.menu as MenuBuilder).setOptionalIconsVisible(true)
    }

    companion object {
        @JvmStatic
        fun newInstance() = TimetableFragment()
    }
}
