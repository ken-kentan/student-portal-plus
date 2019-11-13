package jp.kentan.studentportalplus.ui.notices

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.vo.NoticeQuery
import jp.kentan.studentportalplus.databinding.DialogNoticeFilterBinding

class NoticesFilterDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val BUNDLE_NOTICE_QUERY = "NOTICE_QUERY"

        fun newInstance(noticeQuery: NoticeQuery) = NoticesFilterDialogFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_NOTICE_QUERY, noticeQuery)

            arguments = bundle
        }
    }

    interface Listener {
        fun onFilterApplyClick(noticeQuery: NoticeQuery)
    }

    private lateinit var listener: Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val noticeQuery = requireNotNull(
            requireArguments().getParcelable<NoticeQuery>(BUNDLE_NOTICE_QUERY)
        )

        val context = requireContext()
        val binding = DialogNoticeFilterBinding.inflate(LayoutInflater.from(context)).apply {
            query = noticeQuery
            dateRangeSpinner.adapter = NoticeQueryDateRangeAdapter(context)
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.title_filter_dialog)
            .setPositiveButton(R.string.action_apply) { _, _ ->
                val query = noticeQuery.copy(
                    dateRange = binding.dateRangeSpinner.selectedItem as NoticeQuery.DateRange,
                    isUnread = binding.unreadChip.isChecked,
                    isRead = binding.readChip.isChecked,
                    isFavorite = binding.favoriteChip.isChecked
                )

                listener.onFilterApplyClick(query)
            }
            .setNegativeButton(R.string.action_no, null)
            .setView(binding.root)
            .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = requireParentFragment() as Listener
    }
}
