package jp.kentan.studentportalplus.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityLoginBinding
import jp.kentan.studentportalplus.ui.MainActivity
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.util.hideSoftInput
import javax.inject.Inject

class LoginActivity : DaggerAppCompatActivity() {

    companion object {
        private const val EXTRA_SHOULD_LAUNCH_MAIN_ACTIVITY = "SHOULD_LAUNCH_MAIN_ACTIVITY"

        fun createIntent(context: Context, shouldLaunchMainActivity: Boolean) =
            Intent(context, LoginActivity::class.java).apply {
                putExtra(EXTRA_SHOULD_LAUNCH_MAIN_ACTIVITY, shouldLaunchMainActivity)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val loginViewModel by viewModels<LoginViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login).apply {
            lifecycleOwner = this@LoginActivity
            viewModel = loginViewModel

            setSupportActionBar(toolbar)
        }

        loginViewModel.finishActivity.observe(this) { shouldLaunchMainActivity ->
            if (shouldLaunchMainActivity) {
                startActivity(MainActivity.createIntent(this))
            }

            finish()
        }
        loginViewModel.hideSoftInput.observeEvent(this) {
            hideSoftInput()
        }

        loginViewModel.onActivityCreate(
            intent.getBooleanExtra(EXTRA_SHOULD_LAUNCH_MAIN_ACTIVITY, false)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
