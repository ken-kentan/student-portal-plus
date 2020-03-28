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
import jp.kentan.studentportalplus.util.isPdf

class CustomTabsUrlSpan(
    private val context: Context,
    url: String
) : URLSpan(url) {

    // TODO injection ?
    private val localPreferences = LocalPreferences(context)

    override fun onClick(widget: View) {
        var url = this.url

        if (url.isPdf() && localPreferences.isPdfOpenWithGdocsEnabled) {
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
