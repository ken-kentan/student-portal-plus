package jp.kentan.student_portal_plus.ui.span;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.text.style.URLSpan;
import android.view.View;

import org.chromium.customtabsclient.shared.CustomTabsHelper;

import jp.kentan.student_portal_plus.R;

@SuppressLint("ParcelCreator")
public class CustomTabsURLSpan extends URLSpan {

    private final SharedPreferences mPreference;
    private final String GOOGLE_DOCS_URL;

    public CustomTabsURLSpan(Context context, String url) {
        super(url);
        mPreference = context.getSharedPreferences("common", Context.MODE_PRIVATE);
        GOOGLE_DOCS_URL = context.getString(R.string.url_gdocs);
    }

//    public CustomTabsURLSpan(Context context, Parcel src) {
//        super(src);
//        this.mPreference = context.getSharedPreferences("common", Context.MODE_PRIVATE);
//    }

    @Override
    public void onClick(View view) {
        String url = this.getURL();

        if (mPreference.getBoolean("pdf_open_with_gdocs", true) && url.endsWith(".pdf")) {
            url = GOOGLE_DOCS_URL + url;
        }

        String packageName = CustomTabsHelper.getPackageNameToUse(view.getContext());

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

        builder.setShowTitle(true);
        builder.addDefaultShareMenuItem();
        builder.setToolbarColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage(packageName);
        customTabsIntent.launchUrl(view.getContext(), Uri.parse(url));
    }
}
