package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.ui.viewmodel.NoticeViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.CustomTransformationMethod
import jp.kentan.studentportalplus.util.htmlToSpanned
import jp.kentan.studentportalplus.util.indefiniteSnackbar
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.content_notice.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.find
import org.jetbrains.anko.longToast
import javax.inject.Inject

class NoticeActivity : AppCompatActivity() {

    private companion object {
        const val ROTATION_FROM =   0f
        const val ROTATION_TO   = 144f
        const val DURATION      = 800L
        val INTERPOLATOR = OvershootInterpolator()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(NoticeViewModel::class.java)
    }

    private val customTransformationMethod = CustomTransformationMethod(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        AndroidInjection.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initFab()

        viewModel.notice.observe(this, Observer { data ->
            if (data == null) {
                longToast(getString(R.string.error_not_found, getString(R.string.name_notice)))
                finish()
                return@Observer
            }

            toolbar_layout.title = data.title
            initView(data)
        })

        viewModel.noticeId.value = intent.getLongExtra("id", 0)
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

    private fun initView(data: Notice) {
        fab.rotation = if (data.isFavorite) ROTATION_TO else ROTATION_FROM
        fab.setImageResource(if (data.isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)

        in_charge.text = data.inCharge
        category.text = data.category

        find<TextView>(R.id.title).text = data.title

        if (data.detailHtml != null) {
            detail.text = data.detailHtml.htmlToSpanned()
            detail.transformationMethod = customTransformationMethod
        } else {
            detail_header.visibility = View.GONE
            detail.visibility   = View.GONE
        }

        if (data.link != null) {
            link.text = data.link
            link.transformationMethod = customTransformationMethod
        } else {
            link_header.visibility = View.GONE
            link.visibility   = View.GONE
        }

        date.text = getString(R.string.text_created_date_notice, data.createdDate.toShortString())
    }

    private fun initFab() {
        fab.setOnClickListener {
            viewModel.onClickFavorite{ success: Boolean, favorite: Boolean ->
                if (success) {
                    fab.setImageResource(if (favorite) R.drawable.ic_star else R.drawable.ic_star_border)
                    fab.animate()
                            .rotation(if (favorite) ROTATION_TO else ROTATION_FROM)
                            .setDuration(DURATION)
                            .setInterpolator(INTERPOLATOR)
                            .start()

                    snackbar(it, if (favorite) R.string.msg_set_favorite else R.string.msg_reset_favorite)
                } else {
                    indefiniteSnackbar(it,
                            getString(R.string.error_update, getString(R.string.name_favorite)),
                            getString(R.string.action_close))
                }
            }
        }
    }
}
