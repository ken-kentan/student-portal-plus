package jp.kentan.student_portal_plus.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;


public class ConvertUtils {
    public static float dpToPixel(Context context, float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
