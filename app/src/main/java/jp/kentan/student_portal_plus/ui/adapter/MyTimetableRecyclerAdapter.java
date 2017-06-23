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
import java.util.List;

import jp.kentan.student_portal_plus.ui.MyClassActivity;
import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.util.StringUtils;

public class MyTimetableRecyclerAdapter extends RecyclerView.Adapter<MyTimetableRecyclerAdapter.ViewHolder> {

    private final static int TYPE_DASHBOARD = 0;
    private final static int TYPE_LIST      = 1;
    private final static int TYPE_WEEK      = 2;
    private final static int TYPE_INDEX      = 3;

    private static Drawable IC_LOCK_ON, IC_LOCK_OFF;

    private int mViewType;

    private List<MyClass> mMyClassList = new ArrayList<>();
    private int mListSize = 0;
    private Context mContext;

    public MyTimetableRecyclerAdapter(Context context, final int viewType) {
        super();
        mContext = context;
        mViewType = viewType;

        IC_LOCK_ON  = AppCompatResources.getDrawable(mContext, R.drawable.ic_lock_on);
        IC_LOCK_OFF = AppCompatResources.getDrawable(mContext, R.drawable.ic_lock_off);
    }

    public void updateDataList(List<MyClass> list) {
        mListSize = list.size();

        if(mViewType == TYPE_WEEK){
            mMyClassList.clear();
            mMyClassList.addAll(list);

            notifyDataSetChanged();
        }else{
            final DiffCallback diffCallback = new DiffCallback((mViewType == TYPE_DASHBOARD), mMyClassList, list);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            mMyClassList.clear();
            mMyClassList.addAll(list);

            diffResult.dispatchUpdatesTo(this);
        }
    }

    public void setViewType(int viewType) {
        mViewType = viewType;
    }

    @Override
    public int getItemViewType(final int position) {
        if(mViewType == TYPE_WEEK){
            if(mMyClassList.get(position) == null){
                return TYPE_INDEX;
            }
            return TYPE_WEEK;
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

    @Override
    public void onBindViewHolder(final ViewHolder vh, final int index) {
        MyClass myClass = null;

        if (mViewType == TYPE_WEEK) {
//            final int dayOfWeek = index % 6, period = (index / 6) + 1;
//
//            MyClass tmp;
//            for(int i=0; i<mListSize; ++i) {
//                tmp = mMyClassList.get(i);
//
//                if(tmp.equalTimetable(dayOfWeek, period)){
//                    myClass = tmp;
//                    break;
//                }
//            }

            myClass = mMyClassList.get(index);

            if (myClass == null) {
                vh.mView.setVisibility(View.GONE);
                return;
            }
        } else {
            myClass = mMyClassList.get(index);
        }


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
                vh.mLayout.setBackgroundColor(myClass.getColor());
                break;
            case TYPE_INDEX:
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
            case TYPE_INDEX:
                v = layoutInflater.inflate(R.layout.card_flat_timetable_index, parent, false);
                break;
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

        //Day
        ImageView mIcon;
        View mViewColorHeader;
        TextView mTextViewDatAndPeriod;


        ViewHolder(final Context context, View v) {
            super(v);
            mView = v;

            mLayout = (RelativeLayout) v.findViewById(R.id.layout);
            mTextViewSubject = (TextView) v.findViewById(R.id.subject);
            mTextViewInstructor = (TextView) v.findViewById(R.id.instructor);

            mIcon = (ImageView) v.findViewById(R.id.icon);
            mViewColorHeader = v.findViewById(R.id.color_header);
            mTextViewDatAndPeriod = (TextView) v.findViewById(R.id.day_and_period);

            mTextViewPeriod = (TextView)v.findViewById(R.id.date);
            mTextViewPlace  = (TextView)v.findViewById(R.id.place);
            mSeparator = v.findViewById(R.id.separator);


            mView.setOnClickListener(new View.OnClickListener() {
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
            return sOldList.get(oldItemPosition).getId() == sNewList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            final MyClass oldItem = sOldList.get(oldItemPosition);
            final MyClass newItem = sNewList.get(newItemPosition);

            return oldItem.equals(newItem) && !hasChangedListSize;
        }
    }
}
