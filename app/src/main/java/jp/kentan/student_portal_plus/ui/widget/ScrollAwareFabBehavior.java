package jp.kentan.student_portal_plus.ui.widget;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.view.View;


public class ScrollAwareFabBehavior extends FloatingActionButton.Behavior{

    private FloatingActionButton.OnVisibilityChangedListener mListener;

    public ScrollAwareFabBehavior(FloatingActionButton.OnVisibilityChangedListener listener){
        super();

        mListener = listener;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, final FloatingActionButton fab, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, fab, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0 && fab.getVisibility() == View.VISIBLE) {
            fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    fab.setVisibility(View.INVISIBLE);
                }
            });
        } else if (dyConsumed < 0 && fab.getVisibility() == View.INVISIBLE) {
            fab.show(mListener);
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
}