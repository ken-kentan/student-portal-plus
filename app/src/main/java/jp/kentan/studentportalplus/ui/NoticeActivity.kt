package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.ui.viewmodel.NoticeViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.content_notice.*
import org.jetbrains.anko.toast
import javax.inject.Inject

class NoticeActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
        AndroidInjection.inject(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onStart() {
        super.onStart()

        val viewModel = ViewModelProvider(this, viewModelFactory).get(NoticeViewModel::class.java)

        val id = intent.getLongExtra("id", -1)
        if (id < 0) {
            toast("Invalid Notice id: $id")
            finish()
            return
        }

        viewModel.getNotice(id).observe(this, Observer {
            if (it == null) {
                toast("Not found")
                return@Observer
            }

            title = it.title //TODO not work
            detail_text.text = it.detail
        })
    }
}
