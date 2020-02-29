package jp.kentan.studentportalplus.ui.noticedetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityNoticeDetailBinding
import javax.inject.Inject

class NoticeDetailActivity : DaggerAppCompatActivity() {

    companion object {
        private const val EXTRA_NOTICE_ID = "NOTICE_ID"

        fun createIntent(context: Context, id: Long) =
            Intent(context, NoticeDetailActivity::class.java).apply {
                putExtra(EXTRA_NOTICE_ID, id)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val noticeDetailViewModel by viewModels<NoticeDetailViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityNoticeDetailBinding>(
            this,
            R.layout.activity_notice_detail
        ).apply {
            lifecycleOwner = this@NoticeDetailActivity
            viewModel = noticeDetailViewModel

            setSupportActionBar(toolbar)
        }

        noticeDetailViewModel.finishWithNotFoundError.observe(this) {
            Toast.makeText(this, R.string.all_not_found_error, Toast.LENGTH_LONG).show()
            finish()
        }

        noticeDetailViewModel.onActivityCreate(
            id = intent.getLongExtra(EXTRA_NOTICE_ID, 0)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
