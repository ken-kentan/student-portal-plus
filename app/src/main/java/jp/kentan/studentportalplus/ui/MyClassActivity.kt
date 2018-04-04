package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.ui.viewmodel.MyClassViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.activity_my_class.*
import kotlinx.android.synthetic.main.content_my_class.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.toast
import javax.inject.Inject

class MyClassActivity : AppCompatActivity() {

    private companion object {
        const val SYLLABUS_URI = "http://www.syllabus.kit.ac.jp/?c=detail&schedule_code="
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MyClassViewModel::class.java)
    }

    private var hasUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_class)
        AndroidInjection.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        hasUpdate = false

        viewModel.get(intent.getLongExtra("id", 0)).observe(this, Observer { data ->
            if (data == null) {
                toast(getString(R.string.error_not_found, getString(R.string.name_my_class)))
                finish()
                return@Observer
            }

            if (!hasUpdate) {
                toolbar_layout.title = data.subject
                toolbar_layout.backgroundColor = data.color
                initView(data)

                hasUpdate = true
            }

            if (!data.isUser) {
                fab.hide()
            } else {
                fab.show()
            }

            fab.setOnClickListener {
                //TODO start activity
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initView(data: MyClass) {
        subject_text.text     = data.subject
        instructor_text.text  = data.instructor.format()
        location_text.text    = data.location.format()
        category_text.text    = data.category.format()
        week_period_text.text = getString(R.string.text_week_period, data.week.fullDisplayName.formatWeek(), data.period.formatPeriod())
        syllabus_text.text    = data.scheduleCode.toSyllabusUri()
    }

    private fun String?.format() = if (this.isNullOrBlank()) "未入力" else this

    private fun String.formatWeek() = this.replace("曜日", "曜")

    private fun Int.formatPeriod() = if (this > 0) "${this}限" else ""

    private fun String.toSyllabusUri() = if (this.isBlank()) "未入力" else SYLLABUS_URI + this
}
