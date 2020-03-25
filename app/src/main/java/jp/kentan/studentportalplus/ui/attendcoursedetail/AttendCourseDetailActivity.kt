package jp.kentan.studentportalplus.ui.attendcoursedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.text.parseAsHtml
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityAttendCourseDetailBinding
import jp.kentan.studentportalplus.ui.editattendcourse.EditAttendCourseActivity
import jp.kentan.studentportalplus.ui.observeEvent
import javax.inject.Inject

class AttendCourseDetailActivity : DaggerAppCompatActivity() {

    companion object {
        private const val EXTRA_ID = "ID"

        fun createIntent(context: Context, id: Long) =
            Intent(context, AttendCourseDetailActivity::class.java).apply {
                putExtra(EXTRA_ID, id)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val attendCourseDetailViewModel by viewModels<AttendCourseDetailViewModel> { viewModelFactory }

    private var isEnabledDeleteOptionMenu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityAttendCourseDetailBinding>(
            this,
            R.layout.activity_attend_course_detail
        ).apply {
            lifecycleOwner = this@AttendCourseDetailActivity
            viewModel = attendCourseDetailViewModel

            setSupportActionBar(toolbar)
        }

        attendCourseDetailViewModel.enabledDeleteOptionMenu.observe(this) {
            isEnabledDeleteOptionMenu = true
            invalidateOptionsMenu()
        }
        attendCourseDetailViewModel.startEditAttendCourseActivity.observeEvent(this) {
            startActivity(EditAttendCourseActivity.createIntent(this, it))
        }
        attendCourseDetailViewModel.showDeleteDialog.observeEvent(this) { subject ->
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.all_delete)
                .setMessage(
                    getString(
                        R.string.attend_course_detail_delete_confirm,
                        subject
                    ).parseAsHtml()
                )
                .setPositiveButton(R.string.all_delete) { _, _ ->
                    attendCourseDetailViewModel.onDeleteConfirmClick()
                }
                .setNegativeButton(R.string.all_cancel, null)
                .show()
        }
        attendCourseDetailViewModel.error.observeEvent(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        attendCourseDetailViewModel.finish.observe(this) {
            finish()
        }

        attendCourseDetailViewModel.onActivityCreate(
            intent.getLongExtra(EXTRA_ID, 0)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEnabledDeleteOptionMenu) {
            menuInflater.inflate(R.menu.delete, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> attendCourseDetailViewModel.onDeleteClick()
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
