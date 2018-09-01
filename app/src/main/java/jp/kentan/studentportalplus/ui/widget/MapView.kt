package jp.kentan.studentportalplus.ui.widget

import android.content.Context
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import androidx.core.net.toUri
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.CustomTabsHelper
import jp.kentan.studentportalplus.util.enabledPdfOpenWithGdocs
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.newTask

class MapView {

    enum class Type {CAMPUS, ROOM}

    companion object {
        fun open(context: Context, type: Type) {
            val urlStr = when (type) {
                Type.CAMPUS -> {
                    context.getString(R.string.url_campus_map)
                }
                Type.ROOM -> {
                    if (context.defaultSharedPreferences.enabledPdfOpenWithGdocs()) {
                        context.getString(R.string.url_gdocs, context.getString(R.string.url_room_map))
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

            customTabs.run {
                intent.`package` = CustomTabsHelper.getPackageNameToUse(context)
                intent.newTask()
                launchUrl(context, urlStr.toUri())
            }
        }
    }
}