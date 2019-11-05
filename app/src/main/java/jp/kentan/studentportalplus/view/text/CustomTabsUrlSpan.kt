package jp.kentan.studentportalplus.view.text

import android.content.Context
import android.text.style.URLSpan
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.util.CustomTabsHelper
import jp.kentan.studentportalplus.util.buildCustomTabsIntent

class CustomTabsUrlSpan(
    private val context: Context, url: String
) : URLSpan(url) {

    private val localPreferences = LocalPreferences(context)

    override fun onClick(widget: View) {
        val rawUrl = this.url

        val isPdf = rawUrl.endsWith(".pdf", true)

        var url = rawUrl
        if (isPdf && localPreferences.isEnabledPdfOpenWithGdocs) {
            val isRequireLogin = rawUrl.startsWith("https://portal.student.kit.ac.jp", true)

            if (isRequireLogin) {
                Toast.makeText(context, R.string.error_gdocs_require_login, Toast.LENGTH_LONG)
                    .show()
            } else {
                url = context.getString(R.string.url_gdocs, rawUrl)
            }
        }

        context.buildCustomTabsIntent().run {
            intent.`package` = CustomTabsHelper.getPackageNameToUse(context)
            launchUrl(context, url.toUri())
        }
    }
}