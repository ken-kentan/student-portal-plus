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

import java.util.List;

import jp.kentan.student_portal_plus.ui.NewsActivity;
import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.component.News;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder> {

    private final static int TYPE_DASHBOARD = 0;
    private final static int TYPE_LIST      = 1;

    private static Drawable IC_FAVORITE_ON, IC_FAVORITE_OFF;

    private int mViewType, mListSize;

    private List<News> mNewsList = null;
    private Context mContext;

    private int mLimit = -1;


    public NewsRecyclerAdapter(Context context, List<News> list, final int viewType) {
        super();

        mContext = context;
        mNewsList = list;
        mListSize = (list == null) ? 0 : list.size();
        mViewType = viewType;

        IC_FAVORITE_ON  = AppCompatResources.getDrawable(mContext, R.drawable.ic_favorite_on);
        IC_FAVORITE_OFF = AppCompatResources.getDrawable(mContext, R.drawable.ic_favorite_off);
    }

    public NewsRecyclerAdapter(Context context, List<News> list, final int viewType, final int limit) {
        this(context, list, viewType);

        mLimit = limit;
    }

    public void updateDataList(List<News> list) {
        final DiffCallback diffCallback = new DiffCallback((mViewType == TYPE_DASHBOARD), mNewsList, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        mNewsList = list;
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
        vh.bind(mNewsList.get(index));
    }

    @Override
    public NewsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View v = null;

        switch (viewType) {
            case TYPE_LIST:
                v = layoutInflater.inflate(R.layout.list_news, parent, false);
                break;
            case TYPE_DASHBOARD:
                v = layoutInflater.inflate(R.layout.list_small_news, parent, false);
                break;
        }
        return new ViewHolder(mContext, v, viewType);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private News mNews = null;

        private View mView;
        private ImageView mIcon;
        private TextView mDate, mTitle, mDetail;

        ViewHolder(final Context context, View v, int viewType) {
            super(v);
            mView = v;

            mIcon = (ImageView) v.findViewById(R.id.icon);

            mDate   = (TextView) v.findViewById(R.id.date);
            mTitle  = (TextView) v.findViewById(R.id.title);
            mDetail = (TextView) v.findViewById(R.id.detail);


            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mNews == null) return;

                    mNews.setRead(true);

                    mDate.setTypeface(null, Typeface.NORMAL);
                    mTitle.setTypeface(null, Typeface.NORMAL);
                    if(mDetail != null) mDetail.setTypeface(null, Typeface.NORMAL);

                    Intent latestInfo = new Intent(context, NewsActivity.class);
                    latestInfo.putExtra("id", mNews.getId());

                    context.startActivity(latestInfo);
                }
            });

            if(viewType == TYPE_DASHBOARD) {
                mIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mNews == null) return;

                        final boolean isFavorite = !mNews.isFavorite();

                        mNews.setFavorite(isFavorite);
                        mIcon.setImageDrawable(isFavorite ? IC_FAVORITE_ON : IC_FAVORITE_OFF);
                    }
                });
            }
        }

        void bind(News news){
            mNews = news;

            mIcon.setImageDrawable(news.isFavorite() ? IC_FAVORITE_ON : IC_FAVORITE_OFF);

            final int TYPEFACE = (news.hasRead()) ? Typeface.NORMAL : Typeface.BOLD;

            mDate.setTypeface(null, TYPEFACE);
            mTitle.setTypeface(null, TYPEFACE);

            mDate.setText(news.getDate());
            mTitle.setText(news.getTitle());

            if(mDetail != null){
                mDetail.setTypeface(null, TYPEFACE);
                mDetail.setText(news.getDetailText());
            }
        }
    }


    private static class DiffCallback extends DiffUtil.Callback{
        private static List<News> sOldList = null;
        private static List<News> sNewList = null;

        private static boolean hasChangedListSize;

        DiffCallback(boolean isDashboard, List<News> oldList, List<News> newList) {
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
            final News oldItem = sOldList.get(oldItemPosition);
            final News newItem = sNewList.get(newItemPosition);

            return ((oldItem.hasRead() == newItem.hasRead()) && (oldItem.isFavorite() == newItem.isFavorite())) && !hasChangedListSize;
        }
    }
}
