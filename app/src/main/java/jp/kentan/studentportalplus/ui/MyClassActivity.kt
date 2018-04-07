package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.ui.viewmodel.MyClassViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.indefiniteSnackbar
import jp.kentan.studentportalplus.util.toSpanned
import kotlinx.android.synthetic.main.activity_my_class.*
import kotlinx.android.synthetic.main.content_my_class.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.startActivity
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

    private var isIgnoreUpdate = false
    private var canDelete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_class)
        AndroidInjection.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.get(intent.getLongExtra("id", 0)).observe(this, Observer { data ->
            if (isIgnoreUpdate) {
                return@Observer
            }

            if (data == null) {
                toast(getString(R.string.error_not_found, getString(R.string.name_my_class)))
                finish()
                return@Observer
            }

            toolbar_layout.title = data.subject
            toolbar_layout.backgroundColor = data.color
            initView(data)

            if (!data.isUser) {
                fab.hide()
            } else {
                canDelete = true

                fab.show()
                invalidateOptionsMenu()
            }

            fab.setOnClickListener {
                startActivity<MyClassEditActivity>("id" to data.id)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (canDelete) {
            menuInflater.inflate(R.menu.delete, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home  -> finish()
            R.id.action_delete -> showDeleteDialog()
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

    private fun showDeleteDialog() {
        if (!canDelete) {
            return
        }

        AlertDialog.Builder(this)
                .setTitle(R.string.title_delete)
                .setMessage(getString(R.string.text_delete_confirm, subject_text.text).toSpanned())
                .setPositiveButton(R.string.action_yes) { _, _ ->
                    async(UI) {
                        isIgnoreUpdate = true

                        val success = viewModel.delete().await()
                        if (success) {
                            finish()
                        } else {
                            isIgnoreUpdate = false
                            indefiniteSnackbar(fab, getString(R.string.error_delete), getString(R.string.action_close))
                        }
                    }
                }
                .setNegativeButton(R.string.action_no, null)
                .show()
    }

    private fun String?.format() = if (this.isNullOrBlank()) "未入力" else this

    private fun String.formatWeek() = this.replace("曜日", "曜")

    private fun Int.formatPeriod() = if (this > 0) "${this}限" else ""

    private fun String.toSyllabusUri() = if (this.isBlank()) "未入力" else SYLLABUS_URI + this
}
