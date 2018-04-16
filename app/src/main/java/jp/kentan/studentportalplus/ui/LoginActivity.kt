package jp.kentan.studentportalplus.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.edit
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethClient
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider
import jp.kentan.studentportalplus.ui.span.CustomTitle
import jp.kentan.studentportalplus.util.hideSoftInput
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.intentFor
import javax.inject.Inject


class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var shibbolethDataProvider: ShibbolethDataProvider

    private var loginJob: Job? = null
    private var isLaunchMainActivity: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        AndroidInjection.inject(this)

        title = CustomTitle(this, getString(R.string.title_activity_login))

        // Set up the login form.
        populateUsername()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        login_button.setOnClickListener { attemptLogin() }

        isLaunchMainActivity = intent.getBooleanExtra("from_welcome", false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        loginJob?.cancel()
        finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        loginJob?.cancel()
        super.onBackPressed()
    }

    private fun populateUsername() {
        val usernameStr = shibbolethDataProvider.getUsername() ?: return

        username.setText(usernameStr)
        password.requestFocus()
    }

    private fun attemptLogin() {
        // Prevent multi jobs
        loginJob?.cancel()

        // Reset errors.
        username.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val usernameStr = username.text.toString()
        val passwordStr = password.text.toString()

        var focusView: View? = null

        // Check for a valid password.
        if (TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_field_required)
            focusView = password
        } else if (!isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_invalid_password)
            focusView = password
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(usernameStr)) {
            username.error = getString(R.string.error_field_required)
            focusView = username
        } else if (!isUsernameValid(usernameStr)) {
            username.error = getString(R.string.error_invalid_username)
            focusView = username
        }

        if (focusView != null) {
            focusView.requestFocus()
            return
        }

        hideSoftInput()

        loginJob = launchLoginJob(usernameStr, passwordStr)
    }

    private fun isUsernameValid(username: String): Boolean = username.startsWith('b') || username.startsWith('m') || username.startsWith('d')

    private fun isPasswordValid(password: String): Boolean = password.length in 8..24

    private fun launchLoginJob(username: String, password: String) = launch(UI) {
        showProgress(true)

        val client = ShibbolethClient(this@LoginActivity, shibbolethDataProvider)

        val result = bg {
            client.auth(username, password)
        }

        val (isSuccess, message) = result.await()

        showProgress(false)

        if (isSuccess) {
            defaultSharedPreferences.edit {
                putBoolean("is_first", false)
            }

            if (isLaunchMainActivity) {
                launchMainActivity()
            }
            finish()
        } else {
            error_text.visibility = View.VISIBLE
            error_text.text = message ?: getString(R.string.error_unknown)
        }
    }

    private fun launchMainActivity() {
        val intent = intentFor<MainActivity>("require_sync" to true)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
    }

    private fun showProgress(isShow: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (isShow) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (isShow) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (isShow) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (isShow) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (isShow) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (isShow) View.VISIBLE else View.GONE
                    }
                })
    }
}
