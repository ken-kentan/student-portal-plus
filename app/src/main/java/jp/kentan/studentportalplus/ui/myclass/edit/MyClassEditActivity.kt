package jp.kentan.studentportalplus.ui.myclass.edit

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.android.colorpicker.ColorPickerDialog
import com.android.colorpicker.ColorPickerSwatch
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassColor
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.databinding.ActivityMyClassEditBinding
import jp.kentan.studentportalplus.ui.span.CustomTitle
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import org.jetbrains.anko.longToast
import javax.inject.Inject

class MyClassEditActivity : AppCompatActivity(), MyClassEditNavigator {

    companion object {
        private const val EXTRA_ID = "id"
        private const val EXTRA_WEEK = "week"
        private const val EXTRA_PERIOD = "period"
        private const val EXTRA_MODE = "mode"

        fun createIntent(context: Context, id: Long) =
                Intent(context, MyClassEditActivity::class.java).apply {
                    putExtra(EXTRA_ID, id)
                    putExtra(EXTRA_MODE, MyClassEditViewModel.Mode.EDIT)
                }

        fun createIntent(context: Context, week: ClassWeekType, period: Int) =
                Intent(context, MyClassEditActivity::class.java).apply {
                    putExtra(EXTRA_WEEK, week)
                    putExtra(EXTRA_PERIOD, period)
                    putExtra(EXTRA_MODE, MyClassEditViewModel.Mode.ADD)
                }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, viewModelFactory).get(MyClassEditViewModel::class.java)
    }

    private lateinit var binding: ActivityMyClassEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_class_edit)

        AndroidInjection.inject(this)

        val mode = intent.getSerializableExtra(EXTRA_MODE) as MyClassEditViewModel.Mode

        supportActionBar?.apply {
            title = CustomTitle(this@MyClassEditActivity, mode.title)
            setHomeAsUpIndicator(R.drawable.ic_close_black)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        when (mode) {
            MyClassEditViewModel.Mode.EDIT -> viewModel.startEdit(
                    intent.getLongExtra(EXTRA_ID, 0)
            )
            MyClassEditViewModel.Mode.ADD -> viewModel.startAdd(
                    intent.getSerializableExtra(EXTRA_WEEK) as ClassWeekType,
                    intent.getIntExtra(EXTRA_PERIOD, 0)
            )
        }

        viewModel.navigator = this
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            R.id.action_save -> viewModel.save()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        if (viewModel.hasEdit()) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.title_confirmation)
                    .setMessage(R.string.text_discard_confirm)
                    .setPositiveButton(R.string.action_yes) { _, _ -> super.finish() }
                    .setNegativeButton(R.string.action_no, null)
                    .show()
            return
        }
        super.finish()
    }

    override fun onErrorValidation(isSubject: Boolean, isCredit: Boolean, isScheduleCode: Boolean) {
        var focusView: View? = null

        if (isSubject) {
            binding.subjectEdit.error = getString(R.string.error_field_required)
            focusView = binding.subjectEdit
        }
        if (isCredit) {
            binding.creditEdit.error = getString(R.string.error_invalid_credit)
            focusView = focusView ?: binding.creditEdit
        }
        if (isScheduleCode) {
            binding.scheduleCodeEdit.error = getString(R.string.error_invalid_schedule_code)
            focusView = focusView ?: binding.scheduleCodeEdit
        }

        focusView?.requestFocus()
    }

    override fun onMyClassSaved(success: Boolean) {
        if (success) {
            super.finish()
        } else {
            longToast(getString(R.string.error_update, getString(R.string.name_attend_lecture)))
        }
    }

    override fun openColorPickerDialog(listener: ColorPickerSwatch.OnColorSelectedListener) {
        val dialog = ColorPickerDialog.newInstance(
                R.string.title_color_picker,
                ClassColor.ALL,
                viewModel.color.get(),
                4,
                ClassColor.size)

        dialog.setOnColorSelectedListener(listener)
        dialog.show(fragmentManager, "ColorPickerDialog")
    }
}
