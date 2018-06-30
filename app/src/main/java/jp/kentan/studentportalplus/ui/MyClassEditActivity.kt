package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.android.colorpicker.ColorPickerDialog
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.ClassColor
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.ui.span.CustomTitle
import jp.kentan.studentportalplus.ui.viewmodel.MyClassEditViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.activity_my_class_edit.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.longToast
import javax.inject.Inject

class MyClassEditActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MyClassEditViewModel::class.java)
    }

    private var editor: MyClass
        get() = viewModel.getEditData()
        set(value) = viewModel.edit(value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_class_edit)

        AndroidInjection.inject(this)

        val isEdit = intent.hasExtra("id")

        supportActionBar?.let {
            it.title = CustomTitle(this, if (isEdit) "Edit" else "Add")
            it.setHomeAsUpIndicator(R.drawable.ic_close_black)
            it.setDisplayHomeAsUpEnabled(true)
        }

        val initData: MyClass

        if (isEdit) {
            val data = viewModel.get(intent.getLongExtra("id", 0))

            if (data == null) {
                longToast(getString(R.string.error_not_found, getString(R.string.name_my_class)))
                finish()
                return
            }

            initData = data
        } else {
            if (!intent.hasExtra("week") || !intent.hasExtra("period")) {
                longToast(getString(R.string.error_unknown))
                finish()
                return
            }

            initData = viewModel.create(
                    intent.getSerializableExtra("week") as ClassWeekType,
                    intent.getIntExtra("period", -1)
            )
        }

        initView(initData)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            R.id.action_save -> {
                attemptSave()
            }
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

    private fun initView(data: MyClass) {
        subject_edit.setText(data.subject)
        instructor_edit.setText(data.instructor)
        location_edit.setText(data.location)
        category_edit.setText(data.category)
        credit_edit.setText(if (data.credit > 0) data.credit.toString() else "")
        schedule_code_edit.setText(data.scheduleCode)

        subject_edit.setOnTextChangedListener { editor = editor.copy(subject = it) }
        instructor_edit.setOnTextChangedListener { editor = editor.copy(instructor = it) }
        location_edit.setOnTextChangedListener { editor = editor.copy(location = it) }
        category_edit.setOnTextChangedListener { editor= editor.copy(category = it) }
        credit_edit.setOnTextChangedListener { editor = editor.copy(credit = it.toIntOrNull() ?: 0) }
        schedule_code_edit.setOnTextChangedListener { editor = editor.copy(scheduleCode = it) }

        val background = color_button.background
        background.setColorFilter(data.color, PorterDuff.Mode.MULTIPLY)
        color_button.background = background
        color_button.setOnClickListener {
            val dialog = ColorPickerDialog.newInstance(
                    R.string.title_color_picker,
                    ClassColor.ALL,
                    editor.color,
                    4,
                    ClassColor.size)

            dialog.setOnColorSelectedListener {color ->
                editor= editor.copy(color = color)
                color_button.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            }
            dialog.show(fragmentManager, "ColorPickerDialog")
        }

        if (data.isUser) {
            viewModel.subjects.observe(this, Observer {
                it?.let { subject_edit.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, it)) }
            })
        } else {
            subject_edit.isEnabled       = false
            instructor_edit.isEnabled    = false
            category_edit.isEnabled      = false
            credit_edit.isEnabled        = false
            schedule_code_edit.isEnabled = false
        }

        initSpinners(data)
    }

    private fun initSpinners(data: MyClass) {
        val layout = android.R.layout.simple_list_item_1

        week_spinner.adapter = ArrayAdapter(this, layout, ClassWeekType.values())
        period_spinner.adapter = ArrayAdapter(this, layout, PeriodType.values())

        week_spinner.setSelection(data.week.ordinal)
        period_spinner.setSelection(if (data.period in 1..7) data.period - 1 else 0)

        if (!data.isUser) {
            week_spinner.isEnabled   = false
            period_spinner.isEnabled = false
            return
        }

        week_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val type = week_spinner.selectedItem as ClassWeekType
                editor = editor.copy(week = type)

                val enable = (type != ClassWeekType.INTENSIVE && type != ClassWeekType.UNKNOWN)
                period_spinner.isEnabled = enable
            }
        }
        period_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val period = period_spinner.selectedItem as PeriodType
                editor = editor.copy(period = period.value)
            }
        }
    }

    private fun attemptSave() {
        var focusView: View? = null

        val subject = subject_edit.text.toString().trim()
        val instructor = instructor_edit.text.toString().trim()
        val location = location_edit.text.toString().trim()
        val week = week_spinner.selectedItem as ClassWeekType
        val period = period_spinner.selectedItem as PeriodType
        val category = category_edit.text.toString().trim()
        val creditStr = credit_edit.text.toString().trim()
        val scheduleCode = schedule_code_edit.text.toString().trim()

        if (subject.isBlank()) {
            subject_edit.error = getString(R.string.error_field_required)
            focusView = subject_edit
        }

        if (creditStr.isNotCredit()) {
            credit_edit.error = getString(R.string.error_invalid_credit)
            focusView = focusView ?: credit_edit
        }

        if (scheduleCode.isNotScheduleCode()) {
            schedule_code_edit.error = getString(R.string.error_invalid_schedule_code)
            focusView = focusView ?: schedule_code_edit
        }

        if (focusView != null) {
            focusView.requestFocus()
            return
        }

        editor = editor.copy(
                subject = subject,
                instructor = instructor,
                location = location,
                week = week,
                period = period.value,
                category = category,
                credit = creditStr.toIntOrNull() ?: 0,
                scheduleCode = scheduleCode
        )

        launch(UI) {
            val success = viewModel.save().await()

            if (success) {
                super.finish()
            } else {
                longToast(getString(R.string.error_update, getString(R.string.name_attend_lecture)))
            }
        }
    }

    private fun TextView.setOnTextChangedListener(f:(s: String)->Unit) {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { f(s.toString()) }
        })
    }

    private fun String.isNotCredit(): Boolean {
        val credit = toIntOrNull() ?: return !isEmpty()
        return credit !in 1..10
    }

    private fun String.isNotScheduleCode(): Boolean {
        val code = toIntOrNull() ?: return !isEmpty()
        return code !in 10000000..1000000000
    }

    enum class PeriodType(val value: Int){
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7);

        private val string: String = "${value}限"

        override fun toString() = string
    }
}
