package jp.kentan.studentportalplus.ui.myclass.detail

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
import jp.kentan.studentportalplus.databinding.ActivityMyClassDetailBinding
import jp.kentan.studentportalplus.ui.ViewModelFactory
import jp.kentan.studentportalplus.ui.myclass.edit.MyClassEditActivity
import javax.inject.Inject

class MyClassDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ID = "ID"

        fun createIntent(context: Context, id: Long) =
                Intent(context, MyClassDetailActivity::class.java).apply {
                    putExtra(EXTRA_ID, id)
                }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, viewModelFactory).get(MyClassDetailViewModel::class.java)
    }

    private lateinit var binding: ActivityMyClassDetailBinding

    private var isEnabledOptionMenu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_class_detail)

        AndroidInjection.inject(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        viewModel.subscribe()
        viewModel.onActivityCreated(intent.getLongExtra(EXTRA_ID, -1))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEnabledOptionMenu) {
            menuInflater.inflate(R.menu.delete, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_delete -> viewModel.onDeleteClick()
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun MyClassDetailViewModel.subscribe() {
        val activity = this@MyClassDetailActivity

        startEditActivity.observe(activity, Observer { id ->
            startActivity(MyClassEditActivity.createIntent(activity, id))
        })

        finishActivity.observe(activity, Observer { finish() })

        enabledDeleteOptionMenu.observe(activity, Observer { isEnabled ->
            isEnabledOptionMenu = isEnabled
            invalidateOptionsMenu()
        })

        showDeleteDialog.observe(activity, Observer { subject ->
            AlertDialog.Builder(activity)
                    .setTitle(R.string.title_delete)
                    .setMessage(getString(R.string.text_delete_confirm, subject).parseAsHtml())
                    .setPositiveButton(R.string.action_yes) { _, _ -> viewModel.onDeleteConfirmClick(subject) }
                    .setNegativeButton(R.string.action_no, null)
                    .show()
        })

        errorDelete.observe(activity, Observer { _ ->
            val snackbar = Snackbar.make(binding.root, R.string.error_delete, Snackbar.LENGTH_INDEFINITE)

            snackbar.setAction(R.string.action_close) { snackbar.dismiss() }
                    .show()
        })

        errorNotFound.observe(activity, Observer {
            Toast.makeText(activity, R.string.error_not_found, Toast.LENGTH_LONG).show()
            finish()
        })
    }
}
