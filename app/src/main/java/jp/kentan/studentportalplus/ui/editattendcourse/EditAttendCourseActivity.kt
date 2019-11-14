package jp.kentan.studentportalplus.ui.editattendcourse

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
import jp.kentan.studentportalplus.databinding.ActivityEditAttendCourseBinding
import jp.kentan.studentportalplus.ui.observeEvent
import javax.inject.Inject


class EditAttendCourseActivity : DaggerAppCompatActivity() {

    private enum class Mode(
        @StringRes val titleId: Int
    ) {
        UPDATE(R.string.title_edit_attend_course_update),
        ADD(R.string.title_edit_attend_course_add)
    }

    companion object {
        private const val EXTRA_MODE = "MODE"
        private const val EXTRA_ID = "ID"
        private const val EXTRA_PERIOD = "PERIOD"
        private const val EXTRA_DAY_OF_WEEK = "DAY_OF_WEEK"

        private const val COLOR_PICKER_COLUMN = 4

        fun createIntent(context: Context, id: Long) =
            Intent(context, EditAttendCourseActivity::class.java).apply {
                putExtra(EXTRA_MODE, Mode.UPDATE)
                putExtra(EXTRA_ID, id)
            }

        fun createIntent(context: Context, period: Period, dayOfWeek: DayOfWeek) =
            Intent(context, EditAttendCourseActivity::class.java).apply {
                putExtra(EXTRA_MODE, Mode.ADD)
                putExtra(EXTRA_PERIOD, period)
                putExtra(EXTRA_DAY_OF_WEEK, dayOfWeek)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val editAttendCourseViewModel by viewModels<EditAttendCourseViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityEditAttendCourseBinding>(
            this,
            R.layout.activity_edit_attend_course
        ).apply {
            lifecycleOwner = this@EditAttendCourseActivity
            viewModel = editAttendCourseViewModel

            setSupportActionBar(toolbar)
        }

        editAttendCourseViewModel.toast.observeEvent(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
        editAttendCourseViewModel.showColorPickerDialog.observeEvent(this) {
            showColorPickerDialog(it)
        }
        editAttendCourseViewModel.showFinishConfirmDialog.observeEvent(this) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_confirm)
                .setMessage(R.string.text_discard_confirm)
                .setPositiveButton(R.string.action_discard) { _, _ -> super.finish() }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }
        editAttendCourseViewModel.finish.observe(this) {
            super.finish()
        }

        val editAttendCourseMode = with(intent.getSerializableExtra(EXTRA_MODE) as Mode) {
            setTitle(titleId)

            return@with when (this) {
                Mode.UPDATE -> EditAttendCourseMode.Update(
                    id = intent.getLongExtra(EXTRA_ID, 0)
                )
                Mode.ADD -> EditAttendCourseMode.Add(
                    period = intent.getSerializableExtra(EXTRA_PERIOD) as Period,
                    dayOfWeek = intent.getSerializableExtra(EXTRA_DAY_OF_WEEK) as DayOfWeek
                )
            }
        }

        editAttendCourseViewModel.onActivityCreate(editAttendCourseMode)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> editAttendCourseViewModel.onSaveClick()
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        editAttendCourseViewModel.onFinish()
    }

    private fun showColorPickerDialog(courseColor: CourseColor) {
        val colorMap = CourseColor.values()
            .associateBy { ContextCompat.getColor(this, it.resId) }

        ColorPickerDialog.newInstance(
            R.string.title_color_picker,
            colorMap.keys.toIntArray(),
            ContextCompat.getColor(this, courseColor.resId),
            COLOR_PICKER_COLUMN,
            colorMap.size
        ).run {
            setOnColorSelectedListener {
                val selectedColor = requireNotNull(colorMap[it])
                editAttendCourseViewModel.onColorSelect(selectedColor)
            }
            show(supportFragmentManager, "color_picker_dialog")
        }
    }
}
