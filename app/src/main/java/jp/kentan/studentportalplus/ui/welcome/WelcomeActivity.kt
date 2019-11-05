package jp.kentan.studentportalplus.ui.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityWelcomeBinding
import jp.kentan.studentportalplus.ui.login.LoginActivity
import jp.kentan.studentportalplus.ui.observeEvent

class WelcomeActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context) = Intent(context, WelcomeActivity::class.java)
    }

    private val welcomeViewModel by viewModels<WelcomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityWelcomeBinding>(this, R.layout.activity_welcome)
            .apply {
                lifecycleOwner = this@WelcomeActivity
                viewModel = welcomeViewModel

                setSupportActionBar(toolbar)
            }

        welcomeViewModel.startLoginActivity.observeEvent(this) {
            startActivity(LoginActivity.createIntent(this, shouldLaunchMainActivity = true))
        }
    }
}
