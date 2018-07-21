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

        web_view.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        web_view.settings.setAppCacheEnabled(false)
        web_view.loadUrl(url)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return true
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
}
