package jp.kentan.studentportalplus.ui.span

import android.content.Context
import android.content.Intent
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.text.style.URLSpan
import android.view.View
import androidx.core.net.toUri
import jp.kentan.studentportalplus.R
import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast

class CustomTabsUrlSpan(private val context: Context, url: String) : URLSpan(url) {

    private val gdocsUrlStr: String = context.getString(R.string.url_gdocs)

    override fun onClick(widget: View?) {
        val isPdf = url.endsWith(".pdf", true)
        val isRequireLogin = url.startsWith("https://portal.student.kit.ac.jp", true)

        var urlStr = url
        if (context.defaultSharedPreferences.getBoolean("enable_pdf_open_with_gdocs", true) && isPdf) {
            if (isRequireLogin) {
                context.longToast(R.string.error_gdocs_require_login)
            } else {
                urlStr = gdocsUrlStr + url
            }
        }

        val customTabs = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .addDefaultShareMenuItem()
                .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .build()

        customTabs.intent.`package` = CustomTabsHelper.getPackageNameToUse(context)
        customTabs.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        customTabs.launchUrl(context, urlStr.toUri())
    }
}