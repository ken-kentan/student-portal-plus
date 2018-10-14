package jp.kentan.studentportalplus.ui.main

import android.content.Context
import androidx.core.net.toUri
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.CustomTabsHelper
import jp.kentan.studentportalplus.util.buildCustomTabsIntent
import jp.kentan.studentportalplus.util.isEnabledPdfOpenWithGdocs
import org.jetbrains.anko.defaultSharedPreferences

class MapHelper {

    enum class Type { CAMPUS, ROOM }

    companion object {
        fun open(context: Context, type: Type) {
            val url = when (type) {
                Type.CAMPUS -> {
                    context.getString(R.string.url_campus_map)
                }
                Type.ROOM -> {
                    if (context.defaultSharedPreferences.isEnabledPdfOpenWithGdocs()) {
                        context.getString(R.string.url_gdocs, context.getString(R.string.url_room_map))
                    } else {
                        context.getString(R.string.url_room_map)
                    }
                }
            }

            context.buildCustomTabsIntent().run {
                intent.`package` = CustomTabsHelper.getPackageNameToUse(context)
                launchUrl(context, url.toUri())
            }
        }
    }
}