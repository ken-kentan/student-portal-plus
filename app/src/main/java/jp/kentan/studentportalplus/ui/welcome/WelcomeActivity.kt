package jp.kentan.studentportalplus.ui.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityWelcomeBinding
import jp.kentan.studentportalplus.ui.login.LoginActivity

class WelcomeActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context) =
                Intent(context, WelcomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
    }

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)

        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val viewModel = ViewModelProviders.of(this).get(WelcomeViewModel::class.java)

        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel.apply {
            startLoginActivity.observe(this@WelcomeActivity, Observer {
                val intent = LoginActivity.createIntent(this@WelcomeActivity, true)
                startActivity(intent)
            })
        }
        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    if (!title.orEmpty().contains(context.getString(R.string.title_terms))) {
                        view.loadUrl(context.getString(R.string.url_terms_local))
                    }
                }
            }
            loadUrl(getString(R.string.url_terms))
        }
    }

    override fun onPause() {
        binding.webView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}
