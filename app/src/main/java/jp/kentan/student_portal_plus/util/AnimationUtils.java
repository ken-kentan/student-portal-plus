package jp.kentan.student_portal_plus.util;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;


public class AnimationUtils {
    private final static Animation FADE_IN = new AlphaAnimation(0.0f, 1.0f);
    private final static Animation FADE_OUT = new AlphaAnimation(1.0f, 0.0f);


    public static Animation fadeIn(final View view){
        FADE_IN.setDuration(180);
        FADE_IN.setStartOffset(360);

        view.setVisibility(View.VISIBLE);

        return FADE_IN;
    }

    public static Animation fadeOut(final View view){
        FADE_OUT.setDuration(180);
        FADE_OUT.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        return FADE_OUT;
    }
}
