package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnticipateOvershootInterpolator
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.ui.viewmodel.LectureCancellationViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.indefiniteSnackbar
import jp.kentan.studentportalplus.util.toShortString
import jp.kentan.studentportalplus.util.toSpanned
import kotlinx.android.synthetic.main.activity_lecture_cancellation.*
import kotlinx.android.synthetic.main.content_lecture_cancellation.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.toast
import javax.inject.Inject

class LectureCancellationActivity : AppCompatActivity() {

    private companion object {
        const val ROTATION_FROM =   0f
        const val ROTATION_TO   = 135f
        const val DURATION      = 800L
        val INTERPOLATOR = AnticipateOvershootInterpolator()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LectureCancellationViewModel::class.java)
    }

    private var hasUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecture_cancellation)
        AndroidInjection.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        hasUpdate = false

        viewModel.get(intent.getLongExtra("id", 0)).observe(this, Observer { data ->
            if (data == null) {
                toast(getString(R.string.error_not_found, getString(R.string.name_lecture_cancel)))
                finish()
                return@Observer
            }

            if (!hasUpdate) {
                toolbar_layout.title = data.subject
                initView(data)

                hasUpdate = true
            }

            if (data.attend == LectureAttendType.PORTAL) {
                fab.hide()
            } else {
                fab.show()
            }

            fab.setOnClickListener {
                if (data.attend == LectureAttendType.USER) {
                    showConfirmationDialog(data.subject)
                } else {
                    async(UI) {
                        val success = viewModel.updateAttendByUser(true).await()

                        if (success) {
                            fab.animate()
                                    .rotation(ROTATION_TO)
                                    .setDuration(DURATION)
                                    .setInterpolator(INTERPOLATOR)
                                    .withLayer()
                                    .start()

                            snackbar(it, R.string.msg_register_class)
                        } else {
                            indefiniteSnackbar(layout,
                                    getString(R.string.error_add, getString(R.string.name_attend_lecture)),
                                    getString(R.string.action_close))
                        }
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                val (subject, text) = viewModel.getShareText(this)

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                intent.putExtra(Intent.EXTRA_TEXT, text)

                startActivity(intent)
            }
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initView(data: LectureCancellation) {
        toolbar_layout.title = data.subject

        subject_text.text    = data.subject
        instructor_text.text = data.instructor
        grade_week_period_text.text =
                getString(R.string.text_grade_week_period,
                        data.grade,
                        data.week.formatWeek(),
                        data.period.formatPeriod())
        cancel_date_text.text = data.cancelDate.toShortString()
        detail_text.text      = data.detailHtml.toSpanned()
        date_text.text        = getString(R.string.text_created_date_lecture_cancel, data.createdDate.toShortString())

        fab.rotation = if (data.attend == LectureAttendType.USER) ROTATION_TO else ROTATION_FROM
    }

    private fun showConfirmationDialog(subject: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_confirmation)
        builder.setMessage(getString(R.string.text_unregister_confirm, subject).toSpanned())
        builder.setPositiveButton(R.string.action_yes) { _, _ ->
            async(UI) {
                val success = viewModel.updateAttendByUser(false).await()

                if (success) {
                    snackbar(layout, R.string.msg_unregister_class)

                    fab.animate()
                            .rotation(ROTATION_FROM)
                            .setDuration(DURATION)
                            .setInterpolator(INTERPOLATOR)
                            .withLayer()
                            .start()
                } else {
                    indefiniteSnackbar(layout
                            , getString(R.string.error_remove, getString(R.string.name_attend_lecture)),
                            getString(R.string.action_close))
                }
            }
        }
        builder.setNegativeButton(R.string.action_no, null)
        builder.show()
    }

    private fun String.formatWeek() = this.replace("曜日", "曜")

    private fun String.formatPeriod() = if (this != "-") this + "限" else ""
}
