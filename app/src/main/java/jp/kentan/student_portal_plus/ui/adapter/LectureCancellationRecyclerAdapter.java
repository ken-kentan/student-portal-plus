package jp.kentan.student_portal_plus.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.kentan.student_portal_plus.ui.LectureCancellationActivity;
import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.component.LectureCancellation;

public class LectureCancellationRecyclerAdapter extends RecyclerView.Adapter<LectureCancellationRecyclerAdapter.ViewHolder> {

    private final static int TYPE_DASHBOARD = 0;
    private final static int TYPE_LIST = 1;

    private static Drawable IC_INFO_ON, IC_INFO_OFF, IC_SIMILAR_ON;

    private int mViewType, mListSize = 0;

    private List<LectureCancellation> mLectureCancelList = new ArrayList<>();
    private Context mContext;

    private int mLimit = -1;


    public LectureCancellationRecyclerAdapter(Context context, int viewType) {
        super();

        mContext = context;
        mViewType = viewType;

        IC_INFO_ON    = AppCompatResources.getDrawable(mContext, R.drawable.ic_info_on);
        IC_INFO_OFF   = AppCompatResources.getDrawable(mContext, R.drawable.ic_info_off);
        IC_SIMILAR_ON = AppCompatResources.getDrawable(mContext, R.drawable.ic_similar_on);
    }

    public LectureCancellationRecyclerAdapter(Context context, int viewType, int limit) {
        this(context, viewType);

        mLimit = limit;
    }

    public void updateDataList(List<LectureCancellation> list) {
        final DiffCallback diffCallback = new DiffCallback((mViewType == TYPE_DASHBOARD), mLectureCancelList, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        mLectureCancelList.clear();
        mLectureCancelList.addAll(list);
        mListSize = list.size();

        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return mViewType;
    }

    @Override
    public int getItemCount() {
        return (mLimit < 0) ? mListSize : Math.min(mLimit, mListSize);
    }

    @Override
    public void onBindViewHolder(final ViewHolder vh, final int index) {
        final LectureCancellation info = mLectureCancelList.get(index);

        if (mViewType == TYPE_DASHBOARD) {
            if (mListSize <= mLimit && index == mListSize - 1) {
                vh.mSeparator.setVisibility(View.GONE);
            }else{
                vh.mSeparator.setVisibility(View.VISIBLE);
            }
        }

        vh.bind(info);
    }

    @Override
    public LectureCancellationRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View v = null;

        switch (viewType) {
            case TYPE_LIST:
                v = layoutInflater.inflate(R.layout.list_lecture_cancel, parent, false);
                break;
            case TYPE_DASHBOARD:
                v = layoutInflater.inflate(R.layout.list_small_lecture_cancel, parent, false);
                break;
        }
        return new ViewHolder(mContext, v);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        private LectureCancellation mInfo = null;

        private View mView;
        private ImageView mIcon;
        private TextView mTextViewDate, mTextViewSubject, mTextViewInstructor;

        View mSeparator;


        ViewHolder(final Context context, View v) {
            super(v);
            mView = v;

            mIcon = (ImageView) v.findViewById(R.id.icon);

            mTextViewDate       = (TextView) v.findViewById(R.id.date);
            mTextViewSubject    = (TextView) v.findViewById(R.id.subject);
            mTextViewInstructor = (TextView) v.findViewById(R.id.instructor);

            mSeparator = v.findViewById(R.id.separator);


            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mInfo == null) return;

                    mInfo.setRead(true);

                    mTextViewDate.setTypeface(null, Typeface.NORMAL);
                    mTextViewSubject.setTypeface(null, Typeface.NORMAL);
                    mTextViewInstructor.setTypeface(null, Typeface.NORMAL);

                    Intent intent = new Intent(context, LectureCancellationActivity.class);
                    intent.putExtra("id", mInfo.getId());

                    context.startActivity(intent);
                }
            });
        }

        void bind(LectureCancellation info){
            mInfo = info;

            mTextViewDate.setText(info.getReleaseDate());
            mTextViewSubject.setText(info.getSubject());
            mTextViewInstructor.setText(info.getCancelDate() + "   " + info.getInstructor());

            if(mIcon != null) {
                switch (info.getMyClassStatus()) {
                    case LectureCancellation.RESISTED_BY_PORTAL:
                    case LectureCancellation.RESISTED_BY_USER:
                        mIcon.setImageDrawable(IC_INFO_ON);
                        break;
                    case LectureCancellation.RESISTED_BY_SIMILAR:
                        mIcon.setImageDrawable(IC_SIMILAR_ON);
                        break;
                    default:
                        mIcon.setImageDrawable(IC_INFO_OFF);
                        break;
                }
            }

            final int TYPEFACE = (info.hasRead()) ? Typeface.NORMAL : Typeface.BOLD;

            mTextViewDate.setTypeface(null, TYPEFACE);
            mTextViewSubject.setTypeface(null, TYPEFACE);
            mTextViewInstructor.setTypeface(null, TYPEFACE);
        }
    }


    private static class DiffCallback extends DiffUtil.Callback{
        private static List<LectureCancellation> sOldList = null;
        private static List<LectureCancellation> sNewList = null;

        private static boolean hasChangedListSize;

        DiffCallback(boolean isDashboard, List<LectureCancellation> oldList, List<LectureCancellation> newList) {
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
            final LectureCancellation oldItem = sOldList.get(oldItemPosition);
            final LectureCancellation newItem = sNewList.get(newItemPosition);

            return ((oldItem.hasRead() == newItem.hasRead()) && (oldItem.getMyClassStatus() == newItem.getMyClassStatus())) && !hasChangedListSize;
        }
    }
}
