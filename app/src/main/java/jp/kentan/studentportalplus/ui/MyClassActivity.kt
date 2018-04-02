package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.ui.viewmodel.MyClassViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.animateFadeIn
import kotlinx.android.synthetic.main.activity_my_class.*
import kotlinx.android.synthetic.main.content_my_class.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.toast
import javax.inject.Inject

class MyClassActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MyClassViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_class)
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
            val data: MyClass

            try {
                data = viewModel.get(id).await()
            } catch (e: Exception) {
                failedLoad()
                return@async
            }

            subject_text.text = data.subject
            instructor_text.text = data.instructor
            location_text.text = data.location
            category_text.text = data.category
            week_period_text.text = data.week.displayName //TODO
            syllabus_text.text = data.scheduleCode //TODO

            layout.animateFadeIn(this@MyClassActivity)
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
        toast(getString(R.string.error_not_found, getString(R.string.name_my_class)))
        finish()
    }
}
