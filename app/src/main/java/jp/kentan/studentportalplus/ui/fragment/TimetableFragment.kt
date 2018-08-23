package jp.kentan.studentportalplus.ui.fragment

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.graphics.Typeface
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.view.*
import android.widget.TextView
import androidx.core.content.edit
import dagger.android.support.AndroidSupportInjection

import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.ui.MyClassActivity
import jp.kentan.studentportalplus.ui.MyClassEditActivity
import jp.kentan.studentportalplus.ui.adapter.MyClassAdapter
import jp.kentan.studentportalplus.ui.viewmodel.TimetableFragmentViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_timetable.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.textColorResource
import javax.inject.Inject

class TimetableFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: TimetableFragmentViewModel
    private lateinit var adapter: MyClassAdapter
    private lateinit var layoutType: TimetableFragmentViewModel.LayoutType

    private val weekViewMap: Map<ClassWeekType, TextView> by lazy {
        mapOf(
                ClassWeekType.MONDAY to monday_header,
                ClassWeekType.TUESDAY to tuesday_header,
                ClassWeekType.WEDNESDAY to wednesday_header,
                ClassWeekType.THURSDAY to thursday_header,
                ClassWeekType.FRIDAY to friday_header
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timetable, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        setHasOptionsMenu(true)

        val context  = requireContext()
        val activity = requireActivity()

        layoutType = TimetableFragmentViewModel.LayoutType.valueOf(
                defaultSharedPreferences.getString("timetable_layout", TimetableFragmentViewModel.LayoutType.WEEK.name)
        )
        if (layoutType == TimetableFragmentViewModel.LayoutType.WEEK) {
            grid_layout.visibility = View.VISIBLE
            list_recycler_view.visibility = View.GONE
        } else {
            grid_layout.visibility = View.GONE
            list_recycler_view.visibility = View.VISIBLE
        }

        viewModel = ViewModelProvider(activity, viewModelFactory).get(TimetableFragmentViewModel::class.java)
        viewModel.setViewType(layoutType)

        adapter = MyClassAdapter(context, layoutType.viewType, {
            val intent = MyClassActivity.createIntent(requireContext(), it.id)
            startActivity(intent)
        }, { period: Int, week: ClassWeekType ->
            startActivity<MyClassEditActivity>("period" to period, "week" to week)
        })

        viewModel.getResults().observe(this, Observer {
            note.visibility = if (it == null || it.isEmpty()) View.VISIBLE else View.GONE

            val today = viewModel.getWeek()
            weekViewMap.forEach { (week, view) ->
                if (week == today) {
                    view.typeface = Typeface.DEFAULT_BOLD
                    view.textColorResource = R.color.colorAccent
                } else {
                    view.typeface = Typeface.DEFAULT
                    view.textColorResource = R.color.colorPrimary
                }
            }

            if (layoutType == TimetableFragmentViewModel.LayoutType.WEEK) {
                TransitionManager.beginDelayedTransition(grid_layout)
            }

            adapter.submitList(it)
        })

        // Initialize RecyclerViews
        grid_recycler_view.layoutManager = GridLayoutManager(context, 5)
        grid_recycler_view.adapter = adapter
        grid_recycler_view.isNestedScrollingEnabled = false
        grid_recycler_view.setHasFixedSize(true)
        grid_recycler_view.itemAnimator = null

        list_recycler_view.layoutManager = LinearLayoutManager(context)
        list_recycler_view.adapter = adapter
        list_recycler_view.setHasFixedSize(true)

        activity.onAttachFragment(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.timetable, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_add -> startActivity<MyClassEditActivity>("period" to 1, "week" to ClassWeekType.MONDAY)
            R.id.action_switch_layout -> showLayoutSelectPopup()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("RestrictedApi")
    private fun showLayoutSelectPopup() {
        val anchor: View = requireActivity().find(R.id.action_switch_layout)

        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.popup_switch_layout, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_view_week -> { switchViewType(TimetableFragmentViewModel.LayoutType.WEEK) }
                R.id.action_view_day  -> { switchViewType(TimetableFragmentViewModel.LayoutType.DAY) }
            }
            return@setOnMenuItemClickListener true
        }
        popup.show()

        (popup.menu as MenuBuilder).setOptionalIconsVisible(true)
    }

    private fun switchViewType(type: TimetableFragmentViewModel.LayoutType) {
        if (type == layoutType) {
            return
        }
        layoutType = type

        if (type == TimetableFragmentViewModel.LayoutType.WEEK) {
            grid_layout.visibility = View.VISIBLE
            list_recycler_view.visibility = View.GONE
        } else {
            grid_layout.visibility = View.GONE
            list_recycler_view.visibility = View.VISIBLE
        }

        adapter.setViewType(type.viewType)
        viewModel.setViewType(type)

        defaultSharedPreferences.edit {
            putString("timetable_layout", type.name)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = TimetableFragment()
    }
}
