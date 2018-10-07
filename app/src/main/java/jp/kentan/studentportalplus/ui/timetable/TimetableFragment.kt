package jp.kentan.studentportalplus.ui.timetable

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassWeek
import jp.kentan.studentportalplus.databinding.FragmentTimetableBinding
import jp.kentan.studentportalplus.ui.ViewModelFactory
import jp.kentan.studentportalplus.ui.main.FragmentType
import jp.kentan.studentportalplus.ui.main.MainViewModel
import jp.kentan.studentportalplus.ui.myclass.detail.MyClassDetailActivity
import jp.kentan.studentportalplus.ui.myclass.edit.MyClassEditActivity
import jp.kentan.studentportalplus.util.setGridTimetableLayout
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Inject

class TimetableFragment : Fragment() {

    companion object {
        fun newInstance() = TimetableFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentTimetableBinding
    private lateinit var viewModel: TimetableViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_timetable, container, false)
        binding.setLifecycleOwner(this)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        setHasOptionsMenu(true)

        val provider = ViewModelProvider(requireActivity(), viewModelFactory)

        viewModel = provider.get(TimetableViewModel::class.java)

        val adapter = MyClassAdapter(layoutInflater, viewModel::onClick, viewModel::onAddClick)

        binding.viewModel = viewModel
        binding.gridRecyclerView.apply {
            setAdapter(adapter)
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            itemAnimator = null
        }
        binding.listRecyclerView.apply {
            setAdapter(adapter)
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        viewModel.subscribe(adapter)

        // Call MainViewModel::onAttachFragment
        provider.get(MainViewModel::class.java)
                .onAttachFragment(FragmentType.TIMETABLE)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onFragmentResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.timetable, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_add -> viewModel.onAddClick(ClassWeek.MONDAY, 1)
            R.id.action_switch_layout -> showLayoutSelectPopup()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun TimetableViewModel.subscribe(adapter: MyClassAdapter) {
        val fragment = this@TimetableFragment

        // Should call before adapter::submitList
        isGridLayout.observe(fragment, Observer { isGrid ->
            requireActivity().defaultSharedPreferences.setGridTimetableLayout(isGrid)

            adapter.isGridLayout = isGrid
        })

        myClassList.observe(fragment, Observer { list ->
            adapter.updateCalender()
            adapter.submitList(list)
        })

        dayOfWeek.observe(fragment, Observer { updateWeekHeaders(it) })

        notifyDataSetChanged.observe(fragment, Observer {
            adapter.updateCalender()
            adapter.notifyDataSetChanged()
        })

        startDetailActivity.observe(fragment, Observer { id ->
            startActivity(MyClassDetailActivity.createIntent(requireContext(), id))
        })

        startAddActivity.observe(fragment, Observer { (week, period) ->
            startActivity(MyClassEditActivity.createIntent(requireContext(), week, period))
        })
    }

    @SuppressLint("RestrictedApi")
    private fun showLayoutSelectPopup() {
        val anchor: View = requireActivity().findViewById(R.id.action_switch_layout)

        val popup = PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.popup_switch_layout, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_view_week -> viewModel.onWeekLayoutClick()
                    R.id.action_view_list -> viewModel.onListLayoutClick()
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }

        (popup.menu as MenuBuilder).setOptionalIconsVisible(true)
    }

    private fun updateWeekHeaders(today: ClassWeek) {
        binding.apply {
            mondayHeader.setToday(today == ClassWeek.MONDAY)
            tuesdayHeader.setToday(today == ClassWeek.TUESDAY)
            wednesdayHeader.setToday(today == ClassWeek.WEDNESDAY)
            thursdayHeader.setToday(today == ClassWeek.THURSDAY)
            fridayHeader.setToday(today == ClassWeek.FRIDAY)
        }
    }

    private fun TextView.setToday(isToday: Boolean) {
        if (isToday) {
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        } else {
            typeface = Typeface.DEFAULT
            setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        }
    }
}
