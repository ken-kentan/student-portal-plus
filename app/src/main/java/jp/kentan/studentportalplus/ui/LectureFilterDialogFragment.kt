package jp.kentan.studentportalplus.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.databinding.DialogLectureFilterBinding

class LectureFilterDialogFragment(
    private val lectureQuery: LectureQuery,
    private val onApplyClick: (LectureQuery) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        val binding = DialogLectureFilterBinding.inflate(LayoutInflater.from(context)).apply {
            query = lectureQuery
        }

        return MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setTitle(R.string.title_filter_dialog)
            .setPositiveButton(R.string.action_apply) { _, _ ->
                val query = lectureQuery.copy(
                    isUnread = binding.unreadChip.isChecked,
                    isRead = binding.readChip.isChecked,
                    isAttend = binding.attendChip.isChecked
                )

                onApplyClick(query)
            }
            .setNegativeButton(R.string.action_no, null)
            .create()
    }
}
