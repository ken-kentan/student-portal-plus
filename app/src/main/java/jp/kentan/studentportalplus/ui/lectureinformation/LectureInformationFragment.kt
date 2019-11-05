package jp.kentan.studentportalplus.ui.lectureinformation

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
import jp.kentan.studentportalplus.ui.lectureinformationdetail.LectureInformationDetailActivity
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.view.widget.DividerItemDecoration
import javax.inject.Inject

class LectureInformationFragment : Fragment(R.layout.fragment_list) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val lectureInfoViewModel by activityViewModels<LectureInformationViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lectureInfoAdapter = LectureInformationAdapter(lectureInfoViewModel.onItemClick)

        FragmentListBinding.bind(view).recyclerView.apply {
            adapter = lectureInfoAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext()))
        }

        lectureInfoViewModel.lectureInfoList.observe(
            viewLifecycleOwner,
            lectureInfoAdapter::submitList
        )

        lectureInfoViewModel.startDetailActivity.observeEvent(viewLifecycleOwner) {
            startActivity(LectureInformationDetailActivity.createIntent(requireContext(), it))
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}