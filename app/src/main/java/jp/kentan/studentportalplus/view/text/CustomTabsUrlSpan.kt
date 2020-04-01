package jp.kentan.studentportalplus.view.text

import android.content.Context
import android.text.style.URLSpan
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.CustomTabsHelper
import jp.kentan.studentportalplus.util.buildCustomTabsIntent
import jp.kentan.studentportalplus.util.isPdf

class CustomTabsUrlSpan(
    private val context: Context,
    url: String
) : URLSpan(url) {

    companion object {
        private const val IS_PDF_OPEN_WITH_GDOCS_ENABLED = "is_pdf_open_with_gdocs_enabled"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun onClick(widget: View) {
        var url = this.url

        if (url.isPdf() && sharedPreferences.getBoolean(IS_PDF_OPEN_WITH_GDOCS_ENABLED, true)) {
            val isRequireLogin = url.startsWith("https://portal.student.kit.ac.jp", true)

            if (isRequireLogin) {
                Toast.makeText(context, R.string.custom_tabs_gdocs_require_login, Toast.LENGTH_LONG)
                    .show()
            } else {
                url = context.getString(R.string.custom_tabs_gdocs_url, url)
            }
        }

        context.buildCustomTabsIntent().run {
            intent.`package` = CustomTabsHelper.getPackageNameToUse(context)
            launchUrl(context, url.toUri())
        }
    }
}
