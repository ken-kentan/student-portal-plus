package jp.kentan.student_portal_plus.ui.span;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;


public class CustomTitle extends SpannableString {
    private final static String TYPE_FACE = "Orkney-Medium.otf";

    public CustomTitle(Context context, String title) {
        super(title);
        setSpan(new TypefaceSpan(context, TYPE_FACE), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
