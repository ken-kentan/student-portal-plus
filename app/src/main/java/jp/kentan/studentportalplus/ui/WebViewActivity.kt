package jp.kentan.studentportalplus.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebSettings
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.ui.span.CustomTitle
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val title = intent.getStringExtra("title")
        val url = intent.getStringExtra("url")

        if (title == null || url == null) {
            Log.w("WebViewActivity", "Invalid intent.")
            finish()
            return
        }

        setTitle(CustomTitle(this, title))

        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.setAppCacheEnabled(false)
        webView.loadUrl(url)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return true
    }
}
