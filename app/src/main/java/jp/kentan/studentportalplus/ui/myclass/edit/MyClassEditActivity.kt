package jp.kentan.studentportalplus.ui.myclass.edit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.colorpicker.ColorPickerDialog
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassColor
import jp.kentan.studentportalplus.data.component.ClassWeek
import jp.kentan.studentportalplus.databinding.ActivityMyClassEditBinding
import jp.kentan.studentportalplus.ui.ViewModelFactory
import javax.inject.Inject

class MyClassEditActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ID = "ID"
        private const val EXTRA_WEEK = "WEEK"
        private const val EXTRA_PERIOD = "PERIOD"

        fun createIntent(context: Context, id: Long) =
                Intent(context, MyClassEditActivity::class.java).apply {
                    putExtra(EXTRA_ID, id)
                }

        fun createIntent(context: Context, week: ClassWeek, period: Int) =
                Intent(context, MyClassEditActivity::class.java).apply {
                    putExtra(EXTRA_WEEK, week)
                    putExtra(EXTRA_PERIOD, period)
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

        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_close_black)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        viewModel.subscribe()

        if (intent.hasExtra(EXTRA_ID)) {
            viewModel.onActivityCreated(intent.getLongExtra(EXTRA_ID, -1))
        } else {
            viewModel.onActivityCreated(
                    intent.getSerializableExtra(EXTRA_WEEK) as ClassWeek,
                    intent.getIntExtra(EXTRA_PERIOD, 1)
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> viewModel.onClickSave()
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        viewModel.onFinish()
    }

    private fun MyClassEditViewModel.subscribe() {
        val activity = this@MyClassEditActivity

        title.observe(activity, Observer { resId ->
            setTitle(resId)
        })

        finishActivity.observe(activity, Observer { super.finish() })

        @Suppress("DEPRECATION")
        showColorPickerDialog.observe(activity, Observer { listener ->
            val dialog = ColorPickerDialog.newInstance(
                    R.string.title_color_picker,
                    ClassColor.ALL,
                    viewModel.color.get(),
                    4,
                    ClassColor.size)

            dialog.setOnColorSelectedListener(listener)
            dialog.show(fragmentManager, "ColorPickerDialog")
        })

        showFinishConfirmDialog.observe(activity, Observer {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.title_confirm)
                    .setMessage(R.string.text_discard_confirm)
                    .setPositiveButton(R.string.action_yes) { _, _ -> super.finish() }
                    .setNegativeButton(R.string.action_no, null)
                    .show()
        })

        validation.observe(activity, Observer { result ->
            var focusView: View? = null

            if (result.isSubject) {
                binding.subjectLayout.error = getString(R.string.error_field_required)
                focusView = binding.subject
            }
            if (result.isCredit) {
                binding.creditLayout.error = getString(R.string.error_invalid_credit)
                focusView = focusView ?: binding.credit
            }
            if (result.isScheduleCode) {
                binding.scheduleCodeLayout.error = getString(R.string.error_invalid_schedule_code)
                focusView = focusView ?: binding.scheduleCode
            }

            focusView?.requestFocus()
        })

        errorSaveFailed.observe(activity, Observer {
            Toast.makeText(activity, R.string.error_update, Toast.LENGTH_SHORT).show()
        })

        errorNotFound.observe(activity, Observer {
            Toast.makeText(activity, R.string.error_not_found, Toast.LENGTH_LONG).show()
            finish()
        })
    }
}
