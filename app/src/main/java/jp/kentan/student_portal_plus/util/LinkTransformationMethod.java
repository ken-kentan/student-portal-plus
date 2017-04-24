package jp.kentan.student_portal_plus.util;

import android.content.Context;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.TransformationMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import jp.kentan.student_portal_plus.ui.span.CustomTabsURLSpan;

public class LinkTransformationMethod implements TransformationMethod {

    private Context mContext;
    private final static int LINK_OPTION = Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES;


    public LinkTransformationMethod(Context context) {
        this.mContext = context;
    }

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            Linkify.addLinks(textView, LINK_OPTION);
            if (textView.getText() == null || !(textView.getText() instanceof Spannable)) {
                return source;
            }
            Spannable text = (Spannable) textView.getText();
            URLSpan[] spans = text.getSpans(0, textView.length(), URLSpan.class);
            for (int i = spans.length - 1; i >= 0; i--) {
                URLSpan oldSpan = spans[i];
                int start = text.getSpanStart(oldSpan);
                int end = text.getSpanEnd(oldSpan);
                String url = oldSpan.getURL();
                if (!Patterns.WEB_URL.matcher(url).matches()) {
                    continue;
                }
                text.removeSpan(oldSpan);
                text.setSpan(new CustomTabsURLSpan(mContext, url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return text;
        }
        return source;
    }

    @Override
    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {

    }
}