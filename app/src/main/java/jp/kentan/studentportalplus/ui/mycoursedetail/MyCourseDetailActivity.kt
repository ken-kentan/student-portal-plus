package jp.kentan.studentportalplus.ui.mycoursedetail

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
import jp.kentan.studentportalplus.databinding.ActivityMyCourseDetailBinding
import jp.kentan.studentportalplus.ui.editmycourse.EditMyCourseActivity
import jp.kentan.studentportalplus.ui.observeEvent
import javax.inject.Inject

class MyCourseDetailActivity : DaggerAppCompatActivity() {

    companion object {
        private const val EXTRA_ID = "ID"

        fun createIntent(context: Context, id: Long) =
            Intent(context, MyCourseDetailActivity::class.java).apply {
                putExtra(EXTRA_ID, id)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val myCourseDetailViewModel by viewModels<MyCourseDetailViewModel> { viewModelFactory }

    private var isEnabledDeleteOptionMenu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityMyCourseDetailBinding>(
            this,
            R.layout.activity_my_course_detail
        ).apply {
            lifecycleOwner = this@MyCourseDetailActivity
            viewModel = myCourseDetailViewModel

            setSupportActionBar(toolbar)
        }

        myCourseDetailViewModel.enabledDeleteOptionMenu.observe(this) {
            isEnabledDeleteOptionMenu = true
            invalidateOptionsMenu()
        }
        myCourseDetailViewModel.startEditMyCourseActivity.observeEvent(this) {
            startActivity(EditMyCourseActivity.createIntent(this, it))
        }
        myCourseDetailViewModel.showDeleteDialog.observeEvent(this) { subject ->
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.all_delete)
                .setMessage(
                    getString(
                        R.string.my_course_detail_delete_confirm,
                        subject
                    ).parseAsHtml()
                )
                .setPositiveButton(R.string.all_delete) { _, _ ->
                    myCourseDetailViewModel.onDeleteConfirmClick()
                }
                .setNegativeButton(R.string.all_cancel, null)
                .show()
        }
        myCourseDetailViewModel.error.observeEvent(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        myCourseDetailViewModel.finish.observe(this) {
            finish()
        }

        myCourseDetailViewModel.onActivityCreate(
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
            R.id.action_delete -> myCourseDetailViewModel.onDeleteClick()
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
