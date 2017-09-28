package jp.kentan.student_portal_plus.ui.widget;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.view.View;


public class ScrollAwareFabBehavior extends FloatingActionButton.Behavior{

    private boolean mIsEnabled = false;

    public ScrollAwareFabBehavior(){
        super();
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton fab, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, fab, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);

        if(!mIsEnabled) return;

        if (dyConsumed > 0 && fab.getVisibility() == View.VISIBLE) {
            fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    fab.setVisibility(View.INVISIBLE);
                }
            });
        } else if (dyConsumed < 0 && fab.getVisibility() == View.INVISIBLE) {
            fab.show();
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes, int type) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    public void setEnabled(boolean isEnabled){
        mIsEnabled = isEnabled;
    }
}