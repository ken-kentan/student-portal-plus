package jp.kentan.student_portal_plus.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.kentan.student_portal_plus.ui.LectureInformationActivity;
import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.component.LectureInformation;

public class LectureInformationRecyclerAdapter extends RecyclerView.Adapter<LectureInformationRecyclerAdapter.ViewHolder> {

    private final static int TYPE_DASHBOARD = 0;
    private final static int TYPE_LIST = 1;

    private static Drawable IC_INFO_ON, IC_INFO_OFF, IC_SIMILAR_ON;

    private int mViewType, mListSize;

    private List<LectureInformation> mLectureInfoList = null;
    private Context mContext;

    private int mLimit = -1;

    public LectureInformationRecyclerAdapter(Context context, List<LectureInformation> list, int viewType) {
        super();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        mLectureInfoList = list;
        mListSize = (list == null) ? 0 : list.size();
        mContext = context;
        mViewType = viewType;

        IC_INFO_ON    = AppCompatResources.getDrawable(mContext, R.drawable.ic_info_on);
        IC_INFO_OFF   = AppCompatResources.getDrawable(mContext, R.drawable.ic_info_off);
        IC_SIMILAR_ON = AppCompatResources.getDrawable(mContext, R.drawable.ic_similar_on);
    }

    public LectureInformationRecyclerAdapter(Context context, List<LectureInformation> list, int viewType, int limit) {
        this(context, list, viewType);

        mLimit = limit;
    }

    public void updateDataList(List<LectureInformation> list) {
        final DiffCallback diffCallback = new DiffCallback((mViewType == TYPE_DASHBOARD), mLectureInfoList, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        mLectureInfoList = list;
        mListSize = (list == null) ? 0 : list.size();

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
        final LectureInformation info = mLectureInfoList.get(index);

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
    public LectureInformationRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View v = null;

        switch (viewType) {
            case TYPE_LIST:
                v = layoutInflater.inflate(R.layout.list_lecture_info, parent, false);
                break;
            case TYPE_DASHBOARD:
                v = layoutInflater.inflate(R.layout.list_small_lecture_info, parent, false);
                break;
        }
        return new ViewHolder(mContext, v);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private LectureInformation mInfo = null;

        private View mView;
        private ImageView mIcon;
        private TextView mDate, mSubject, mDetail;

        View mSeparator;

        ViewHolder(final Context context, View v) {
            super(v);
            mView = v;

            mIcon = (ImageView) v.findViewById(R.id.icon);

            mDate    = (TextView) v.findViewById(R.id.date);
            mSubject = (TextView) v.findViewById(R.id.subject);
            mDetail  = (TextView) v.findViewById(R.id.detail);

            mSeparator = v.findViewById(R.id.separator);


            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mInfo == null) return;

                    mInfo.setRead(true);

                    mDate.setTypeface(null, Typeface.NORMAL);
                    mSubject.setTypeface(null, Typeface.NORMAL);
                    mDetail.setTypeface(null, Typeface.NORMAL);

                    Intent lectureInfo = new Intent(context, LectureInformationActivity.class);
                    lectureInfo.putExtra("id", mInfo.getId());

                    context.startActivity(lectureInfo);
                }
            });
        }

        void bind(LectureInformation info){
            mInfo = info;

            mDate.setText(info.getUpdateDate());
            mSubject.setText(info.getSubject());
            mDetail.setText(info.getDetailText());

            switch (info.getMyClassStatus()){
                case LectureInformation.RESISTED_BY_PORTAL:
                case LectureInformation.RESISTED_BY_USER:
                    mIcon.setImageDrawable(IC_INFO_ON);
                    break;
                case LectureInformation.RESISTED_BY_SIMILAR:
                    mIcon.setImageDrawable(IC_SIMILAR_ON);
                    break;
                default:
                    mIcon.setImageDrawable(IC_INFO_OFF);
                    break;
            }

            final int TYPEFACE = (info.hasRead()) ? Typeface.NORMAL : Typeface.BOLD;

            mSubject.setTypeface(null, TYPEFACE);
            mDetail.setTypeface(null, TYPEFACE);
        }
    }


    private static class DiffCallback extends DiffUtil.Callback{
        private static List<LectureInformation> sOldList = null;
        private static List<LectureInformation> sNewList = null;

        private static boolean hasChangedListSize;

        DiffCallback(boolean isDashboard, List<LectureInformation> oldList, List<LectureInformation> newList) {
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
            final LectureInformation oldItem = sOldList.get(oldItemPosition);
            final LectureInformation newItem = sNewList.get(newItemPosition);

            return ((oldItem.hasRead() == newItem.hasRead()) && (oldItem.getMyClassStatus() == newItem.getMyClassStatus())) && !hasChangedListSize;
        }
    }
}
