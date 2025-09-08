package jp.kentan.studentportalplus.ui.web

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityWebBinding

class WebActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_URL = "EXTRA_URL"

        fun createIntent(context: Context, title: String, url: String) =
                Intent(context, WebActivity::class.java).apply {
                    putExtra(EXTRA_TITLE, title)
                    putExtra(EXTRA_URL, url)
                }
    }

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = DataBindingUtil.setContentView<ActivityWebBinding>(this, R.layout.activity_web).webView

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val title = intent.getStringExtra(EXTRA_TITLE)
        val url = intent.getStringExtra(EXTRA_URL)

        if (title == null || url == null) {
            finish()
            return
        }

        setTitle(title)
        webView.apply {
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            loadUrl(url)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    override fun onPause() {
        webView.run {
            onPause()
            pauseTimers()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.run {
            resumeTimers()
            onResume()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}