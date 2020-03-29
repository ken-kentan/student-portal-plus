package jp.kentan.studentportalplus.ui.editmycourse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.android.colorpicker.ColorPickerDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.vo.CourseColor
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period
import jp.kentan.studentportalplus.databinding.ActivityEditMyCourseBinding
import jp.kentan.studentportalplus.ui.observeEvent
import javax.inject.Inject

class EditMyCourseActivity : DaggerAppCompatActivity() {

    private enum class Mode(
        @StringRes val titleId: Int
    ) {
        UPDATE(R.string.edit_my_course_edit_title),
        ADD(R.string.edit_my_course_add_title)
    }

    companion object {
        private const val EXTRA_MODE = "MODE"
        private const val EXTRA_ID = "ID"
        private const val EXTRA_PERIOD = "PERIOD"
        private const val EXTRA_DAY_OF_WEEK = "DAY_OF_WEEK"

        private const val COLOR_PICKER_COLUMN = 4

        fun createIntent(context: Context, id: Long) =
            Intent(context, EditMyCourseActivity::class.java).apply {
                putExtra(EXTRA_MODE, Mode.UPDATE)
                putExtra(EXTRA_ID, id)
            }

        fun createIntent(context: Context, period: Period, dayOfWeek: DayOfWeek) =
            Intent(context, EditMyCourseActivity::class.java).apply {
                putExtra(EXTRA_MODE, Mode.ADD)
                putExtra(EXTRA_PERIOD, period)
                putExtra(EXTRA_DAY_OF_WEEK, dayOfWeek)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val editMyCourseViewModel by viewModels<EditMyCourseViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityEditMyCourseBinding>(
            this,
            R.layout.activity_edit_my_course
        ).apply {
            lifecycleOwner = this@EditMyCourseActivity
            viewModel = editMyCourseViewModel

            setSupportActionBar(toolbar)
        }

        editMyCourseViewModel.toast.observeEvent(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
        editMyCourseViewModel.showColorPickerDialog.observeEvent(this, showColorPickerDialog)
        editMyCourseViewModel.showFinishConfirmDialog.observeEvent(this) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.all_confirm)
                .setMessage(R.string.edit_my_course_discard_confirm)
                .setPositiveButton(R.string.all_discard) { _, _ -> super.finish() }
                .setNegativeButton(R.string.all_cancel, null)
                .show()
        }
        editMyCourseViewModel.finish.observe(this) {
            super.finish()
        }

        val editMyCourseMode = with(intent.getSerializableExtra(EXTRA_MODE) as Mode) {
            setTitle(titleId)

            return@with when (this) {
                Mode.UPDATE -> EditMyCourseMode.Update(
                    id = intent.getLongExtra(EXTRA_ID, 0)
                )
                Mode.ADD -> EditMyCourseMode.Add(
                    period = intent.getSerializableExtra(EXTRA_PERIOD) as Period,
                    dayOfWeek = intent.getSerializableExtra(EXTRA_DAY_OF_WEEK) as DayOfWeek
                )
            }
        }

        editMyCourseViewModel.onActivityCreate(editMyCourseMode)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> editMyCourseViewModel.onSaveClick()
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        editMyCourseViewModel.onFinish()
    }

    private val showColorPickerDialog = { courseColor: CourseColor ->
        val colorMap = CourseColor.values()
            .associateBy { ContextCompat.getColor(this, it.resId) }

        ColorPickerDialog.newInstance(
            R.string.edit_my_course_color_picker,
            colorMap.keys.toIntArray(),
            ContextCompat.getColor(this, courseColor.resId),
            COLOR_PICKER_COLUMN,
            colorMap.size
        ).run {
            setOnColorSelectedListener {
                val selectedColor = requireNotNull(colorMap[it])
                editMyCourseViewModel.onCourseColorSelect(selectedColor)
            }
            show(supportFragmentManager, "color_picker_dialog")
        }
    }
}
