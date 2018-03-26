package jp.kentan.studentportalplus.ui

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
import jp.kentan.studentportalplus.ui.viewmodel.NoticeViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
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

        fab.setOnClickListener {
            val favorite = !viewModel.isFavorite()
            viewModel.setFavorite(favorite)

            fab.setImageResource(if (favorite) R.drawable.ic_star else R.drawable.ic_star_border)
            fab.animate()
                    .rotation(if (favorite) START_ROTATION_TO else START_ROTATION_FROM)
                    .setInterpolator(OvershootInterpolator())
                    .setDuration(800)
                    .start()

            snackbar(it, if (favorite) R.string.action_set_favorite else R.string.action_reset_favorite)
        }


        async(UI) {
            val data = viewModel.getNotice(id).await()

            if (data == null) {
                failedLoad()
                return@async
            }

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

            created_date_header.text = getString(R.string.name_created_date_format, data.createdDate.toShortString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                val (subject, text) = viewModel.getNoticeShareText(this)

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
        toast(getString(R.string.error_not_found, getString(R.string.name_notice)))
        finish()
    }
}
