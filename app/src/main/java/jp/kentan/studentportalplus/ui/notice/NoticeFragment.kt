package jp.kentan.studentportalplus.ui.notice

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
import jp.kentan.studentportalplus.ui.noticedetail.NoticeDetailActivity
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.view.widget.DividerItemDecoration
import javax.inject.Inject

class NoticeFragment : Fragment(R.layout.fragment_list) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val noticeViewModel by activityViewModels<NoticeViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val noticeAdapter = NoticeAdapter(noticeViewModel.onItemClick)

        FragmentListBinding.bind(view).recyclerView.apply {
            adapter = noticeAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext()))
        }

        noticeViewModel.lectureInfoList.observe(viewLifecycleOwner, noticeAdapter::submitList)
        noticeViewModel.startDetailActivity.observeEvent(viewLifecycleOwner) {
            startActivity(NoticeDetailActivity.createIntent(requireContext(), it))
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
