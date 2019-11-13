package jp.kentan.studentportalplus.ui.lectures

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.databinding.DialogLectureFilterBinding

class LecturesFilterDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val BUNDLE_LECTURE_QUERY = "LECTURE_QUERY"

        fun newInstance(lectureQuery: LectureQuery) = LecturesFilterDialogFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_LECTURE_QUERY, lectureQuery)

            arguments = bundle
        }
    }

    interface Listener {
        fun onFilterApplyClick(lectureQuery: LectureQuery)
    }

    private lateinit var listener: Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val lectureQuery = requireNotNull(
            requireArguments().getParcelable<LectureQuery>(BUNDLE_LECTURE_QUERY)
        )

        val context = requireContext()
        val binding = DialogLectureFilterBinding.inflate(LayoutInflater.from(context)).apply {
            query = lectureQuery
            orderSpinner.adapter = LectureQueryOrderAdapter(context)
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.title_filter_dialog)
            .setPositiveButton(R.string.action_apply) { _, _ ->
                val query = lectureQuery.copy(
                    order = binding.orderSpinner.selectedItem as LectureQuery.Order,
                    isUnread = binding.unreadChip.isChecked,
                    isRead = binding.readChip.isChecked,
                    isAttend = binding.attendChip.isChecked
                )

                listener.onFilterApplyClick(query)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .setView(binding.root)
            .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = requireParentFragment() as Listener
    }
}
