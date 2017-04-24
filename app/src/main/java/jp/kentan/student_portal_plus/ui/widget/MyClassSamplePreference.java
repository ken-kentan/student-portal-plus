package jp.kentan.student_portal_plus.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.util.JaroWinklerDistance;


public class MyClassSamplePreference extends Preference {

    private final static String[] SUBJECT = {"ABC実験ma", "ABC実験ma~mc", "ABC実験 ガイダンス", "XYZ実験ma"};
    private static Drawable INFO, NOT_INFO, SIMILAR;

    private final static JaroWinklerDistance JARO_WINKLER_DISTANCE = new JaroWinklerDistance();

    private View[] mViews = null;
    private ImageView[] mIconViews = new ImageView[4];

    private float threshold = 0.8f;

    public MyClassSamplePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWidgetLayoutResource(R.layout.sample_my_class_threshold);

        INFO     = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_info_on, null);
        NOT_INFO = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_info_off, null);
        SIMILAR  = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_similar_on, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mViews = new View[]{
                view.findViewById(R.id.info1),
                view.findViewById(R.id.info2),
                view.findViewById(R.id.info3),
                view.findViewById(R.id.info4),
        };


        //Initialize
        int index = 0;
        char indexChar = 'A';
        for(View item : mViews){
            mIconViews[index] = (ImageView)item.findViewById(R.id.icon);
            TextView date       = (TextView)item.findViewById(R.id.date);
            TextView subject    = (TextView)item.findViewById(R.id.subject);
            TextView detail = (TextView)item.findViewById(R.id.detail);

            String strDate = "2017/01/0" + (index+1);
            String strDetail = "詳細テキスト" + (indexChar++);

            date.setText(strDate);
            subject.setText(SUBJECT[index++]);
            detail.setText(strDetail);
        }

        updateView();
    }

    private void updateView(){
        for(int i=1, size=mIconViews.length; i<size; ++i){
            if(JARO_WINKLER_DISTANCE.getDistance(SUBJECT[0], SUBJECT[i]) >= threshold){
                mIconViews[i].setImageDrawable(SIMILAR);
            }else{
                mIconViews[i].setImageDrawable(NOT_INFO);
            }
        }

        mIconViews[0].setImageDrawable(INFO);
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;

        if(mViews != null){
            updateView();
        }
    }
}
