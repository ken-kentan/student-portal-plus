package jp.kentan.studentportalplus.ui.lecturecancellationdetail

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
import jp.kentan.studentportalplus.databinding.ActivityLectureCancellationDetailBinding
import jp.kentan.studentportalplus.ui.observeEvent
import javax.inject.Inject

class LectureCancellationDetailActivity : DaggerAppCompatActivity() {

    companion object {
        private const val EXTRA_LECTURE_CANCELLATION_ID = "LECTURE_CANCELLATION_ID"

        fun createIntent(context: Context, id: Long) =
            Intent(context, LectureCancellationDetailActivity::class.java).apply {
                putExtra(EXTRA_LECTURE_CANCELLATION_ID, id)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val lectureCancelViewModel by viewModels<LectureCancellationDetailViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityLectureCancellationDetailBinding>(
            this,
            R.layout.activity_lecture_cancellation_detail
        ).apply {
            lifecycleOwner = this@LectureCancellationDetailActivity
            viewModel = lectureCancelViewModel

            setSupportActionBar(toolbar)
        }

        lectureCancelViewModel.excludeFromMyConfirmDialog.observeEvent(this) { subject ->
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.all_confirm)
                .setMessage(
                    getString(
                        R.string.all_exclude_from_my_course_confirm,
                        subject
                    ).parseAsHtml()
                )
                .setPositiveButton(R.string.all_exclude) { _, _ ->
                    lectureCancelViewModel.onExcludeConfirmClick(subject)
                }
                .setNegativeButton(R.string.all_cancel, null)
                .show()
        }
        lectureCancelViewModel.finishWithNotFoundError.observe(this) {
            Toast.makeText(this, R.string.all_not_found_error, Toast.LENGTH_LONG).show()
            finish()
        }
        lectureCancelViewModel.snackbar.observeEvent(this) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
        }
        lectureCancelViewModel.indefiniteSnackbar.observeEvent(this) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_INDEFINITE).apply {
                setAction(R.string.all_close) { dismiss() }
            }.show()
        }

        lectureCancelViewModel.onActivityCreate(
            id = intent.getLongExtra(EXTRA_LECTURE_CANCELLATION_ID, 0)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
