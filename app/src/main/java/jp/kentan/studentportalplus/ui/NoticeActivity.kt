package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.ui.viewmodel.NoticeViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.indefiniteSnackbar
import jp.kentan.studentportalplus.util.toShortString
import jp.kentan.studentportalplus.util.toSpanned
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.content_notice.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.toast
import javax.inject.Inject

class NoticeActivity : AppCompatActivity() {

    private companion object {
        const val START_ROTATION_FROM =   0f
        const val START_ROTATION_TO   = 144f
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(NoticeViewModel::class.java)
    }

    private var hasUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        AndroidInjection.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.get(intent.getLongExtra("id", 0)).observe(this, Observer { data ->
            if (data == null) {
                toast(getString(R.string.error_not_found, getString(R.string.name_notice)))
                finish()
                return@Observer
            }

            if (!hasUpdate) {
                toolbar_layout.title = data.title
                initView(data)

                hasUpdate = true
            }

            fab.setOnClickListener {
                async(UI) {
                    val favorite = !data.isFavorite
                    val success = viewModel.updateFavorite(favorite).await()

                    if (success) {
                        fab.setImageResource(if (favorite) R.drawable.ic_star else R.drawable.ic_star_border)
                        fab.animate()
                                .rotation(if (favorite) START_ROTATION_TO else START_ROTATION_FROM)
                                .setInterpolator(OvershootInterpolator())
                                .setDuration(800)
                                .start()

                        snackbar(it, if (favorite) R.string.msg_set_favorite else R.string.msg_reset_favorite)
                    } else {
                        indefiniteSnackbar(it,
                                getString(R.string.error_update, getString(R.string.name_favorite)),
                                getString(R.string.action_close))
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

    private fun initView(data: Notice) {
        fab.rotation = if (data.isFavorite) START_ROTATION_TO else START_ROTATION_FROM
        fab.setImageResource(if (data.isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)

        in_charge_text.text = data.inCharge
        category_text.text = data.category

        title_text.text  = data.title

        if (data.detailHtml != null) {
            detail_text.text = data.detailHtml.toSpanned()
        } else {
            detail_header.visibility = View.GONE
            detail_text.visibility   = View.GONE
        }

        if (data.link != null) {
            link_text.text = data.link
        } else {
            link_header.visibility = View.GONE
            link_text.visibility   = View.GONE
        }

        date_text.text = getString(R.string.text_created_date_notice, data.createdDate.toShortString())
    }
}
