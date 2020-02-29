package jp.kentan.studentportalplus.ui.lectureinformationdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.text.parseAsHtml
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityLectureInformationDetailBinding
import jp.kentan.studentportalplus.ui.observeEvent
import javax.inject.Inject

class LectureInformationDetailActivity : DaggerAppCompatActivity() {

    companion object {
        private const val EXTRA_LECTURE_INFORMATION_ID = "LECTURE_INFORMATION_ID"

        fun createIntent(context: Context, id: Long) =
            Intent(context, LectureInformationDetailActivity::class.java).apply {
                putExtra(EXTRA_LECTURE_INFORMATION_ID, id)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val lectureInfoDetailViewModel by viewModels<LectureInformationDetailViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityLectureInformationDetailBinding>(
            this,
            R.layout.activity_lecture_information_detail
        ).apply {
            lifecycleOwner = this@LectureInformationDetailActivity
            viewModel = lectureInfoDetailViewModel

            setSupportActionBar(toolbar)
        }

        lectureInfoDetailViewModel.excludeFromAttendConfirmDialog.observeEvent(this) { subject ->
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.all_confirm)
                .setMessage(
                    getString(
                        R.string.all_exclude_from_attend_course,
                        subject
                    ).parseAsHtml()
                )
                .setPositiveButton(R.string.all_exclude) { _, _ ->
                    lectureInfoDetailViewModel.onExcludeConfirmClick(subject)
                }
                .setNegativeButton(R.string.all_cancel, null)
                .show()
        }
        lectureInfoDetailViewModel.finishWithNotFoundError.observe(this) {
            Toast.makeText(this, R.string.all_not_found_error, Toast.LENGTH_LONG).show()
            finish()
        }
        lectureInfoDetailViewModel.snackbar.observeEvent(this) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
        }
        lectureInfoDetailViewModel.indefiniteSnackbar.observeEvent(this) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_INDEFINITE).apply {
                setAction(R.string.all_close) { dismiss() }
            }.show()
        }

        lectureInfoDetailViewModel.onActivityCreate(
            id = intent.getLongExtra(EXTRA_LECTURE_INFORMATION_ID, 0)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
