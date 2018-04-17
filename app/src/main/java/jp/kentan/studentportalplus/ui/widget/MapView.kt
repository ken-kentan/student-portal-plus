package jp.kentan.studentportalplus.ui.widget

import android.content.Context
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import androidx.core.net.toUri
import jp.kentan.studentportalplus.R
import org.chromium.customtabsclient.shared.CustomTabsHelper
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.newTask

class MapView {

    enum class Type {CAMPUS, ROOM }

    companion object {
        fun open(context: Context, type: Type) {
            val urlStr = when (type) {
                Type.CAMPUS -> {
                    context.getString(R.string.url_campus_map)
                }
                Type.ROOM -> {
                    if (context.defaultSharedPreferences.getBoolean("pdf_open_with_gdocs", true)) {
                        context.getString(R.string.url_gdocs) + context.getString(R.string.url_room_map)
                    } else {
                        context.getString(R.string.url_room_map)
                    }
                }
            }

            val customTabs = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .addDefaultShareMenuItem()
                    .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .build()

            customTabs.intent.`package` = CustomTabsHelper.getPackageNameToUse(context)
            customTabs.intent.newTask()
            customTabs.launchUrl(context, urlStr.toUri())
        }
    }
}