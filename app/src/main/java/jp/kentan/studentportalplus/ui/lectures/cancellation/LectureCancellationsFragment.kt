package jp.kentan.studentportalplus.ui.lectures.cancellation

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.FragmentListBinding
import jp.kentan.studentportalplus.ui.lecturecancellationdetail.LectureCancellationDetailActivity
import jp.kentan.studentportalplus.ui.lectures.LecturesAdapter
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.view.widget.DividerItemDecoration
import javax.inject.Inject

class LectureCancellationsFragment : Fragment(R.layout.fragment_list) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val lectureCancelsViewModel by activityViewModels<LectureCancellationsViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lecturesAdapter =
            LecturesAdapter(lectureCancelsViewModel.onItemClick)

        FragmentListBinding.bind(view).recyclerView.apply {
            adapter = lecturesAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext()))
        }

        lectureCancelsViewModel.lectureCancelList.observe(
            viewLifecycleOwner,
            lecturesAdapter::submitList
        )

        lectureCancelsViewModel.startDetailActivity.observeEvent(viewLifecycleOwner) {
            startActivity(LectureCancellationDetailActivity.createIntent(requireContext(), it))
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
