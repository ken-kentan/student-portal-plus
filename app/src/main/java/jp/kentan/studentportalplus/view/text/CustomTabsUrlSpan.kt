package jp.kentan.studentportalplus.view.text

import android.content.Context
import android.text.style.URLSpan
import android.view.View
import androidx.core.net.toUri
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.CustomTabsHelper
import jp.kentan.studentportalplus.util.buildCustomTabsIntent
import jp.kentan.studentportalplus.util.isEnabledPdfOpenWithGdocs
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast

class CustomTabsUrlSpan(
        private val context: Context, url: String
) : URLSpan(url) {

    override fun onClick(widget: View) {
        val isPdf = url.endsWith(".pdf", true)

        var urlStr = url
        if (isPdf && context.defaultSharedPreferences.isEnabledPdfOpenWithGdocs()) {
            val isRequireLogin = url.startsWith("https://portal.student.kit.ac.jp", true)

            if (isRequireLogin) {
                context.longToast(R.string.error_gdocs_require_login)
            } else {
                urlStr = context.getString(R.string.url_gdocs, url)
            }
        }

        context.buildCustomTabsIntent().run {
            intent.`package` = CustomTabsHelper.getPackageNameToUse(context)
            launchUrl(context, urlStr.toUri())
        }
    }
}