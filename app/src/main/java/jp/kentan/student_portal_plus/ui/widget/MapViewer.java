package jp.kentan.student_portal_plus.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import jp.kentan.student_portal_plus.R;

public class MapViewer {
    public final static int CAMPUS_MAP = 0;
    public final static int ROOM_MAP = 1;


    public static void show(Context context, int map) {
        final String url;

        switch (map) {
            case CAMPUS_MAP:
                url = context.getString(R.string.url_campus_map);
                break;
            case ROOM_MAP:
                final SharedPreferences pref = context.getSharedPreferences("common", Context.MODE_PRIVATE);

                if (pref.getBoolean("pdf_open_with_gdocs", true)) {
                    url = context.getString(R.string.url_gdocs) + context.getString(R.string.url_room_map);
                } else {
                    url = context.getString(R.string.url_room_map);
                }

                break;
            default:
                return;
        }

        String packageName = CustomTabsHelper.getPackageNameToUse(context);

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

        builder.setShowTitle(true);
        builder.addDefaultShareMenuItem();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage(packageName);
        customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
}
