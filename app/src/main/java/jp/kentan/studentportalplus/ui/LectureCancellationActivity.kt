package jp.kentan.studentportalplus.ui

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
import jp.kentan.studentportalplus.util.animateFadeIn
import jp.kentan.studentportalplus.util.indefiniteSnackbar
import jp.kentan.studentportalplus.util.toShortString
import jp.kentan.studentportalplus.util.toSpanned
import kotlinx.android.synthetic.main.activity_lecture_information.*
import kotlinx.android.synthetic.main.content_lecture_cancellation.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.toast
import javax.inject.Inject

class LectureCancellationActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LectureCancellationViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecture_cancellation)
        AndroidInjection.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()

        val id    = intent.getLongExtra("id", -1)
        val title = intent.getStringExtra("title")
        if (id < 0 || title == null) {
            failedLoad()
            return
        }

        setTitle(title)

        async(UI) {
            val data: LectureCancellation
            try {
                data = viewModel.get(id).await()
            } catch (e: Exception) {
                failedLoad()
                return@async
            }

            subject_text.text = data.subject
            instructor_text.text = data.instructor
            grade_week_period_text.text =
                    getString(R.string.text_grade_week_period,
                            data.grade,
                            data.week.formatWeek(),
                            data.period.formatPeriod())
            cancel_date_text.text = data.cancelDate.toShortString()
            detail_text.text = data.detailHtml.toSpanned()
            date_text.text = getString(R.string.text_created_date_lecture_cancel, data.createdDate.toShortString())

            if (data.attend == LectureAttendType.PORTAL) {
                fab.hide()
            } else {
                fab.rotation = if (data.attend == LectureAttendType.USER) 135f else 0f
                fab.animateFadeIn(this@LectureCancellationActivity)
            }

            fab.setOnClickListener {
                val type = viewModel.getAttendType()

                if (type == LectureAttendType.USER) {
                    showConfirmationDialog(data.subject)
                } else {
                    async(UI) {
                        val (success, message) = viewModel.setAttendByUser(true).await()

                        if (success) {
                            fab.animate()
                                    .rotation(135f)
                                    .setDuration(800)
                                    .setInterpolator(AnticipateOvershootInterpolator())
                                    .withLayer()
                                    .start()

                            snackbar(it, R.string.msg_register_class)
                        } else {
                            indefiniteSnackbar(layout, message, getString(R.string.error_add), getString(R.string.action_close))
                        }
                    }
                }
            }

            layout.animateFadeIn(this@LectureCancellationActivity)
        }
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

    private fun failedLoad() {
        toast(getString(R.string.error_not_found, getString(R.string.name_lecture_info)))
        finish()
    }

    private fun showConfirmationDialog(subject: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_confirmation)
        builder.setMessage(getString(R.string.text_unregister_confirm, subject).toSpanned())
        builder.setPositiveButton(R.string.action_yes) { _, _ ->
            async(UI) {
                val (success, message) = viewModel.setAttendByUser(false).await()

                if (success) {
                    snackbar(layout, R.string.msg_unregister_class)

                    fab.animate()
                            .rotation(0f)
                            .setDuration(800)
                            .setInterpolator(AnticipateOvershootInterpolator())
                            .withLayer()
                            .start()
                } else {
                    indefiniteSnackbar(layout, message, getString(R.string.error_remove), getString(R.string.action_close))
                }
            }
        }
        builder.setNegativeButton(R.string.action_no, null)
        builder.show()
    }

    private fun String.formatWeek() = this.replace("曜日", "曜")

    private fun String.formatPeriod() = if (this != "-") this + "限" else ""
}
