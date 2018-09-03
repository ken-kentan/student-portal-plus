package jp.kentan.studentportalplus.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityMyClassBinding
import jp.kentan.studentportalplus.ui.viewmodel.MyClassViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import jp.kentan.studentportalplus.util.CustomTransformationMethod
import jp.kentan.studentportalplus.util.htmlToSpanned
import jp.kentan.studentportalplus.util.indefiniteSnackbar
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import javax.inject.Inject

class MyClassActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_DATA_ID = "id"

        fun createIntent(context: Context, id: Long): Intent {
            return Intent(context, MyClassActivity::class.java).apply {
                putExtra(EXTRA_DATA_ID, id)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityMyClassBinding

    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, viewModelFactory).get(MyClassViewModel::class.java)
    }

    private val customTransformationMethod = CustomTransformationMethod(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_class)

        AndroidInjection.inject(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.myClass.observe(this, Observer { data ->
            if (data == null) {
                longToast(getString(R.string.error_not_found, getString(R.string.name_my_class)))
                finish()
                return@Observer
            }

            binding.myClass = data
            binding.setOnEditClickListener {
                startActivity<MyClassEditActivity>("id" to data.id)
            }
            binding.content.syllabus.transformationMethod = customTransformationMethod

            invalidateOptionsMenu()
        })

        viewModel.setId(intent.getLongExtra(EXTRA_DATA_ID, 0))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (viewModel.canDelete) {
            menuInflater.inflate(R.menu.delete, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home  -> finish()
            R.id.action_delete -> showDeleteDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog() {
        if (!viewModel.canDelete) {
            return
        }

        AlertDialog.Builder(this)
                .setTitle(R.string.title_delete)
                .setMessage(getString(R.string.text_delete_confirm, viewModel.subject).htmlToSpanned())
                .setPositiveButton(R.string.action_yes) { _, _ ->
                    viewModel.delete { success ->
                        if (success) {
                            finish()
                        } else {
                            indefiniteSnackbar(binding.fab, getString(R.string.error_delete), getString(R.string.action_close))
                        }
                    }
                }
                .setNegativeButton(R.string.action_no, null)
                .show()
    }
}
