package jp.kentan.student_portal_plus.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.kentan.student_portal_plus.ui.MyClassActivity;
import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.util.StringUtils;

public class MyTimetableRecyclerAdapter extends RecyclerView.Adapter<MyTimetableRecyclerAdapter.ViewHolder> {

    private static final int[] CLASS_PERIOD_M = new int[]{(8*60 + 50), (10*60 + 30), (12*60 + 50), (14*60 + 30), (16*60 + 10), (17*60 + 50), (19*60 + 30)};

    private final static int TYPE_DASHBOARD = 0;
    private final static int TYPE_LIST      = 1;
    private final static int TYPE_WEEK      = 2;
    private final static int TYPE_EMPTY     = 3;

    private static Drawable IC_LOCK_ON, IC_LOCK_OFF;

    private static float CELL_HEIGHT_PX;

    private int mViewType;

    private List<MyClass> mMyClassList = new ArrayList<>();
    private int mListSize = 0;
    private Context mContext;

    public MyTimetableRecyclerAdapter(Context context, final int viewType) {
        super();
        mContext = context;
        mViewType = viewType;

        CELL_HEIGHT_PX = (float)mContext.getResources().getDimensionPixelSize(R.dimen.timetable_cell_height);

        IC_LOCK_ON  = AppCompatResources.getDrawable(mContext, R.drawable.ic_lock_on);
        IC_LOCK_OFF = AppCompatResources.getDrawable(mContext, R.drawable.ic_lock_off);
    }

    public void updateDataList(List<MyClass> list) {
        mListSize = list.size();

        final DiffCallback diffCallback = new DiffCallback((mViewType == TYPE_DASHBOARD), mMyClassList, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        mMyClassList.clear();
        mMyClassList.addAll(list);

        diffResult.dispatchUpdatesTo(this);
    }

    public void updateDataListBySilent(List<MyClass> list) {
        mListSize = list.size();

        mMyClassList.clear();
        mMyClassList.addAll(list);

        notifyDataSetChanged();
    }

    public void setViewType(int viewType) {
        mViewType = viewType;
    }

    @Override
    public int getItemViewType(final int position) {
        if(mViewType == TYPE_WEEK){
            return (mMyClassList.get(position) != null) ? TYPE_WEEK : TYPE_EMPTY;
        }
        return mViewType;
    }

    @Override
    public int getItemCount() {
        if (mViewType == TYPE_WEEK) {
            return 35; //7*5
        } else {
            return mListSize;
        }
    }

    private float getTimeProgress(final int index){
        final int dayOfWeek = index % 5 + 2, period = index / 5;
        final Calendar now = Calendar.getInstance();

        final int DAY_OF_WEEK = now.get(Calendar.DAY_OF_WEEK);

        if(dayOfWeek < DAY_OF_WEEK || DAY_OF_WEEK == 1){
            return 1.0f;
        }else if(dayOfWeek == DAY_OF_WEEK){
            final int nowMinute = now.get(Calendar.MINUTE) + now.get(Calendar.HOUR_OF_DAY) * 60;

            //授業開始時間から何分経ったか
            final int diffMinute = nowMinute - CLASS_PERIOD_M[period];

            if(diffMinute > 0){
                float progress = (float)diffMinute / 90.0f;

                return Math.min(progress, 1.0f);
            }
        }

        return 0.0f;
    }

    @Override
    public void onBindViewHolder(final ViewHolder vh, final int index) {
        final MyClass myClass = mMyClassList.get(index);

        switch (mViewType){
            case TYPE_DASHBOARD:
                String strPeriod = Integer.toString(myClass.getPeriod());
                vh.mTextViewPeriod.setText(strPeriod);

                vh.mTextViewInstructor.setVisibility(StringUtils.isEmpty(myClass.getInstructor()) ? View.GONE : View.VISIBLE);

                vh.mSeparator.setVisibility((index == mListSize-1) ? View.GONE : View.VISIBLE);
                break;
            case TYPE_LIST:
                String dayAndPeriod = MyClass.DAY_OF_WEEK[myClass.getDayOfWeek()];

                final int period = myClass.getPeriod();

                if (period >= 1){
                    dayAndPeriod += Integer.toString(period);
                }
                vh.mTextViewDatAndPeriod.setText(dayAndPeriod);

                vh.mIcon.setImageDrawable((myClass.hasRegisteredByUser()) ? IC_LOCK_OFF: IC_LOCK_ON);
                break;
            case TYPE_WEEK:

                final float progress = getTimeProgress(index);

                if(progress > 0.0f){
                    vh.mMask.setVisibility(View.VISIBLE);

                    if(progress < 0.98f){
                        vh.mMask.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(progress * CELL_HEIGHT_PX)));
                        vh.mBorder.setVisibility((progress > 0.98f) ? View.GONE : View.VISIBLE);
                    }else{
                        vh.mMask.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        vh.mBorder.setVisibility(View.GONE);
                    }

                }else{
                    vh.mBorder.setVisibility(View.GONE);
                    vh.mMask.setVisibility(View.INVISIBLE);
                }

                if(myClass == null) return;

                vh.mLayout.setBackgroundColor(myClass.getColor());
                break;
        }

        vh.bind(myClass);
    }

    @Override
    public MyTimetableRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        View v = null;

        switch (viewType) {
            case TYPE_DASHBOARD:
                v = layoutInflater.inflate(R.layout.list_small_my_class, parent, false);
                break;
            case TYPE_LIST:
                v = layoutInflater.inflate(R.layout.list_my_class, parent, false);
                break;
            case TYPE_WEEK:
                v = layoutInflater.inflate(R.layout.card_flat_my_class, parent, false);
                break;
            case TYPE_EMPTY:
                v = layoutInflater.inflate(R.layout.card_flat_empty_class, parent, false);
                return new ViewHolder(null, v);
        }
        return new ViewHolder(mContext, v);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private MyClass mClass = null;

        //Common
        private View mView;
        private TextView mTextViewSubject, mTextViewInstructor, mTextViewPlace;

        //Dashboard
        TextView mTextViewPeriod;
        View mSeparator;

        //Week
        RelativeLayout mLayout;
        View mMask, mBorder;

        //Day
        ImageView mIcon;
        View mViewColorHeader;
        TextView mTextViewDatAndPeriod;


        ViewHolder(final Context context, View v) {
            super(v);
            mView = v;

            mMask   = v.findViewById(R.id.mask);
            mBorder = v.findViewById(R.id.border);

            if(context == null) return;

            mLayout = (RelativeLayout) v.findViewById(R.id.layout);
            mTextViewSubject = (TextView) v.findViewById(R.id.subject);
            mTextViewInstructor = (TextView) v.findViewById(R.id.instructor);

            mIcon = (ImageView) v.findViewById(R.id.icon);
            mViewColorHeader = v.findViewById(R.id.color_header);
            mTextViewDatAndPeriod = (TextView) v.findViewById(R.id.day_and_period);

            mTextViewPeriod = (TextView)v.findViewById(R.id.date);
            mTextViewPlace  = (TextView)v.findViewById(R.id.place);
            mSeparator = v.findViewById(R.id.separator);

            final View clickableView = (mLayout == null) ? mView : mLayout;

            clickableView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mClass == null) return;

                    Intent myClassInfo = new Intent(context, MyClassActivity.class);
                    myClassInfo.putExtra("id", mClass.getId());

                    context.startActivity(myClassInfo);
                }
            });
        }

        void bind(MyClass myClass){
            mClass = myClass;

            mView.setVisibility(View.VISIBLE);

            mTextViewSubject.setText(mClass.getSubject());
            mTextViewInstructor.setText(mClass.getInstructor());

            if(mTextViewPlace != null) mTextViewPlace.setText(mClass.getPlace());
            if(mViewColorHeader != null) mViewColorHeader.setBackgroundColor(mClass.getColor());
        }
    }


    private static class DiffCallback extends DiffUtil.Callback{
        private static List<MyClass> sOldList = null;
        private static List<MyClass> sNewList = null;

        private static boolean hasChangedListSize;

        DiffCallback(boolean isDashboard, List<MyClass> oldList, List<MyClass> newList) {
            sOldList = oldList;
            sNewList = newList;

            hasChangedListSize = isDashboard && (getOldListSize() != getNewListSize());
        }

        @Override
        public int getOldListSize() {
            return (sOldList == null) ? 0 : sOldList.size();
        }

        @Override
        public int getNewListSize() {
            return (sNewList == null) ? 0 : sNewList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            final MyClass oldItem = sOldList.get(oldItemPosition);
            final MyClass newItem = sNewList.get(newItemPosition);

            if(oldItem != null && newItem != null){
                return sOldList.get(oldItemPosition).getId() == sNewList.get(newItemPosition).getId();
            }else{
                return oldItem == null && newItem == null;
            }
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            final MyClass oldItem = sOldList.get(oldItemPosition);
            final MyClass newItem = sNewList.get(newItemPosition);

            if(oldItem != null && newItem != null){
                return oldItem.equals(newItem) && !hasChangedListSize;
            }else{
                return oldItem == null && newItem == null;
            }
        }
    }
}
