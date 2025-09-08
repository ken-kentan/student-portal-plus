package jp.kentan.studentportalplus.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityLoginBinding
import jp.kentan.studentportalplus.ui.ViewModelFactory
import jp.kentan.studentportalplus.ui.main.MainActivity
import jp.kentan.studentportalplus.util.hideSoftInput
import jp.kentan.studentportalplus.util.setAuthenticatedUser
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_IS_LAUNCH_MAIN_ACTIVITY = "IS_LAUNCH_MAIN_ACTIVITY"

        fun createIntent(context: Context, isLaunchMainActivity: Boolean) =
                Intent(context, LoginActivity::class.java).apply {
                    putExtra(EXTRA_IS_LAUNCH_MAIN_ACTIVITY, isLaunchMainActivity)
                }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityLoginBinding

    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)
    }

    private var isLaunchMainActivity: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        AndroidInjection.inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isLaunchMainActivity = intent.getBooleanExtra(EXTRA_IS_LAUNCH_MAIN_ACTIVITY, false)

        binding.setLifecycleOwner(this)
        binding.password.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                viewModel.onLoginClick()
                return@setOnEditorActionListener true
            }
            false
        }
        binding.viewModel = viewModel

        viewModel.subscribe()
        viewModel.onActivityCreated()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        viewModel.cancelLogin()
        super.finish()
    }

    private fun LoginViewModel.subscribe() {
        val activity = this@LoginActivity

        loginSuccess.observe(activity, Observer {
            activity.defaultSharedPreferences.setAuthenticatedUser(true)

            if (isLaunchMainActivity) {
                startActivity(MainActivity.createIntent(activity, isSync = true))
            }
            finish()
        })

        validation.observe(activity, Observer { result ->
            var focusView: View? = null

            if (result.isEmptyUsername) {
                binding.usernameLayout.error = getString(R.string.error_field_required)
                focusView = binding.username
            } else if (result.isInvalidUsername) {
                binding.usernameLayout.error = getString(R.string.error_invalid_username)
                focusView = binding.username
            }

            if (result.isEmptyPassword) {
                binding.passwordLayout.error = getString(R.string.error_field_required)
                focusView = focusView ?: binding.password
            } else if (result.isInvalidPassword) {
                binding.passwordLayout.error = getString(R.string.error_invalid_password)
                focusView = focusView ?: binding.password
            }

            focusView?.requestFocus()
        })

        hideSoftInput.observe(activity, Observer {
            activity.hideSoftInput()
        })
    }
}
