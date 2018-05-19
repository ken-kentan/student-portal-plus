package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnticipateOvershootInterpolator
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.ui.viewmodel.LectureInformationViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.CustomTransformationMethod
import jp.kentan.studentportalplus.util.htmlToSpanned
import jp.kentan.studentportalplus.util.indefiniteSnackbar
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.activity_lecture_information.*
import kotlinx.android.synthetic.main.content_lecture_information.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.longToast
import javax.inject.Inject


class LectureInformationActivity : AppCompatActivity() {

    private companion object {
        const val ROTATION_FROM =   0f
        const val ROTATION_TO   = 135f
        const val DURATION      = 800L
        val INTERPOLATOR = AnticipateOvershootInterpolator()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(LectureInformationViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecture_information)
        AndroidInjection.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.lectureInfo.observe(this, Observer<LectureInformation> { data ->
            if (data == null) {
                longToast(getString(R.string.error_not_found, getString(R.string.name_lecture_info)))
                finish()
                return@Observer
            }

            toolbar_layout.title = data.subject
            initView(data)

            if (data.attend == LectureAttendType.PORTAL) {
                fab.hide()
            } else {
                fab.show()
            }

            fab.setOnClickListener {
                val type = viewModel.getCurrentAttendType() ?: return@setOnClickListener

                if (type == LectureAttendType.USER) {
                    showConfirmationDialog(data.subject)
                } else {
                    viewModel.onClickAttendToUser { success ->
                        if (success) {
                            fab.animate()
                                    .rotation(ROTATION_TO)
                                    .setDuration(DURATION)
                                    .setInterpolator(INTERPOLATOR)
                                    .withLayer()
                                    .start()

                            snackbar(it, R.string.msg_register_class)
                        } else {
                            indefiniteSnackbar(it,
                                    getString(R.string.error_add, getString(R.string.name_attend_lecture)),
                                    getString(R.string.action_close))
                        }
                    }
                }
            }
        })

        viewModel.lectureInfoId.value = intent.getLongExtra("id", 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> viewModel.onClickShare(this)
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initView(data: LectureInformation) {
        subject.text = data.subject
        instructor.text = data.instructor
        semester_week_period.text =
                getString(R.string.text_semester_week_period,
                        data.grade,
                        data.semester.formatSemester(),
                        data.week.formatWeek(),
                        data.period.formatPeriod())
        category.text = data.category
        detail.text = data.detailHtml.htmlToSpanned()
        detail.transformationMethod = CustomTransformationMethod(this)
        date.text = getString(R.string.text_created_date_lecture_info, data.createdDate.toShortString())

        if (data.createdDate != data.updatedDate) {
            date.append(getString(R.string.text_updated_date_lecture_info, data.updatedDate.toShortString()))
        }

        fab.rotation = if (data.attend == LectureAttendType.USER) ROTATION_TO else ROTATION_FROM
    }

    private fun showConfirmationDialog(subject: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_confirmation)
        builder.setMessage(getString(R.string.text_unregister_confirm, subject).htmlToSpanned())
        builder.setPositiveButton(R.string.action_yes) { _, _ ->
            viewModel.onClickAttendToNot { success ->
                if (success) {
                    snackbar(layout, R.string.msg_unregister_class)

                    fab.animate()
                            .rotation(ROTATION_FROM)
                            .setDuration(DURATION)
                            .setInterpolator(AnticipateOvershootInterpolator())
                            .withLayer()
                            .start()
                } else {
                    indefiniteSnackbar(layout,
                            getString(R.string.error_remove, getString(R.string.name_attend_lecture)),
                            getString(R.string.action_close))
                }
            }
        }
        builder.setNegativeButton(R.string.action_no, null)
        builder.show()
    }

    private fun String.formatSemester() = if (this == "前" || this == "後") this + "学期" else this.hyphenToWhitespace()

    private fun String.formatWeek() = this.replace("曜日", "曜")

    private fun String.formatPeriod() = if (this != "-") this + "限" else ""

    private fun String.hyphenToWhitespace() = if (this == "-") " " else this
}
