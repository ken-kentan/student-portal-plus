package jp.kentan.studentportalplus.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityWebBinding
import jp.kentan.studentportalplus.ui.span.CustomTitle

class WebActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_URL = "url"

        fun createIntent(context: Context, title: String, url: String)
                = Intent(context, WebActivity::class.java).apply {
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
            Log.w("WebActivity", "Invalid intent.")
            finish()
            return
        }

        setTitle(CustomTitle(this, title))

        webView.apply {
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.setAppCacheEnabled(false)
            loadUrl(url)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
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
