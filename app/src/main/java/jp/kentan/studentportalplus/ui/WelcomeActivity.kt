package jp.kentan.studentportalplus.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.ui.span.CustomTitle
import kotlinx.android.synthetic.main.activity_welcome.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast


class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        title = CustomTitle(this, getString(R.string.title_activity_welcome))

        setupWebView()

        shibboleth_button.setOnClickListener {
            if (!agree_checkbox.isChecked) {
                longToast(getString(R.string.error_not_agree_to_terms))
                return@setOnClickListener
            }

            startActivity(intentFor<LoginActivity>("request_launch_main_activity" to true))
        }
    }

    override fun onPause() {
        web_view.onPause()
        web_view.pauseTimers()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        web_view.resumeTimers()
        web_view.onResume()
    }

    override fun onDestroy() {
        web_view.destroy()
        super.onDestroy()
    }

    private fun setupWebView() {
        web_view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                view ?: return

                if (!view.title.contains("")) {
                    web_view.loadUrl("local")
                }
            }
        }
        web_view.loadUrl(getString(R.string.url_terms))
    }
}
