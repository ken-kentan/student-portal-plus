package jp.kentan.studentportalplus.ui.lecturecancel.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.parseAsHtml
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityLectureCancelDetailBinding
import jp.kentan.studentportalplus.ui.ViewModelFactory
import javax.inject.Inject

class LectureCancelDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ID = "ID"

        fun createIntent(context: Context, id: Long) =
                Intent(context, LectureCancelDetailActivity::class.java).apply {
                    putExtra(EXTRA_ID, id)
                }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, viewModelFactory).get(LectureCancelDetailViewModel::class.java)
    }

    private lateinit var binding: ActivityLectureCancelDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lecture_cancel_detail)

        AndroidInjection.inject(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        viewModel.subscribe()
        viewModel.onActivityCreated(intent.getLongExtra(EXTRA_ID, -1))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> viewModel.onShareClick()
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun LectureCancelDetailViewModel.subscribe() {
        val activity = this@LectureCancelDetailActivity

        showAttendNotDialog.observe(activity, Observer { subject ->
            AlertDialog.Builder(activity)
                    .setTitle(R.string.title_confirm)
                    .setMessage(getString(R.string.text_unregister_confirm, subject).parseAsHtml())
                    .setPositiveButton(R.string.action_yes) { _, _ ->
                        viewModel.onAttendNotClick(subject)
                    }
                    .setNegativeButton(R.string.action_no, null)
                    .show()
        })

        snackbar.observe(activity, Observer { resId ->
            Snackbar.make(binding.root, resId, Snackbar.LENGTH_SHORT)
                    .show()
        })

        indefiniteSnackbar.observe(activity, Observer { resId ->
            val snackbar = Snackbar.make(binding.root, resId, Snackbar.LENGTH_INDEFINITE)

            snackbar.setAction(R.string.action_close) { snackbar.dismiss() }
                    .show()
        })

        share.observe(activity, Observer { startActivity(it) })

        errorNotFound.observe(activity, Observer {
            Toast.makeText(activity, R.string.error_not_found, Toast.LENGTH_LONG).show()
            finish()
        })
    }
}
