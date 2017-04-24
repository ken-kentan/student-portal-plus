package jp.kentan.student_portal_plus.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import jp.kentan.student_portal_plus.ui.HomeActivity;
import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.data.component.LectureCancellation;
import jp.kentan.student_portal_plus.data.component.LectureInformation;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.ui.adapter.LectureCancellationRecyclerAdapter;
import jp.kentan.student_portal_plus.ui.adapter.MyTimetableRecyclerAdapter;
import jp.kentan.student_portal_plus.ui.adapter.NewsRecyclerAdapter;
import jp.kentan.student_portal_plus.ui.adapter.LectureInformationRecyclerAdapter;
import jp.kentan.student_portal_plus.ui.span.CustomTitle;


public class DashboardFragment extends Fragment {

    private final static int TYPE_DASHBOARD = 0;

    private MyTimetableRecyclerAdapter mMyTimetableRecyclerAdapter;
    private LectureInformationRecyclerAdapter mLectureInfoAdapter;
    private LectureCancellationRecyclerAdapter mLectureCancelAdapter;
    private NewsRecyclerAdapter mNewsAdapter;

    private CardView mCardViewTimetable, mCardViewLectureInfo, mCardViewLectureCancel, mCardViewNews;
    private View mViewTimetable, mViewLectureInfo, mViewLectureCancel, mViewNews;

    private TextView mHeaderLectureInfo, mHeaderLectureCancel;
    private TextView mButtonLectureInfo, mButtonLectureCancel;
    private TextView mTextViewLectureInfo, mTextViewLectureCancel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();

        mMyTimetableRecyclerAdapter = new MyTimetableRecyclerAdapter(context, null, TYPE_DASHBOARD);
        mLectureInfoAdapter         = new LectureInformationRecyclerAdapter(context, null, TYPE_DASHBOARD, 3);
        mLectureCancelAdapter       = new LectureCancellationRecyclerAdapter(context, null, TYPE_DASHBOARD, 3);
        mNewsAdapter                = new NewsRecyclerAdapter(context, null, TYPE_DASHBOARD, 3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        /*
        UI Initialize
         */
        final HomeActivity activity = (HomeActivity) getActivity();
        final View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        RecyclerView timetable     = (RecyclerView) view.findViewById(R.id.timetable);
        RecyclerView lectureInfo   = (RecyclerView) view.findViewById(R.id.recycler_view_lecture_info);
        RecyclerView lectureCancel = (RecyclerView) view.findViewById(R.id.recycler_view_lecture_cancel);
        RecyclerView news          = (RecyclerView) view.findViewById(R.id.recycler_view_news);

        mCardViewTimetable     = (CardView) view.findViewById(R.id.card_view_timetable);
        mCardViewLectureInfo   = (CardView) view.findViewById(R.id.card_view_lecture_info);
        mCardViewLectureCancel = (CardView) view.findViewById(R.id.card_view_lecture_cancel);
        mCardViewNews          = (CardView) view.findViewById(R.id.card_view_news);

        mHeaderLectureInfo   = (TextView) view.findViewById(R.id.header_lecture_info);
        mHeaderLectureCancel = (TextView) view.findViewById(R.id.header_lecture_cancel);

        mButtonLectureInfo   = (TextView) view.findViewById(R.id.button_lecture_info);
        mButtonLectureCancel = (TextView) view.findViewById(R.id.button_lecture_cancel);


        setupRecyclerView(timetable, mMyTimetableRecyclerAdapter);
        setupRecyclerView(lectureCancel, mLectureCancelAdapter);
        setupRecyclerView(lectureInfo, mLectureInfoAdapter);
        setupRecyclerView(news, mNewsAdapter);

        mViewTimetable     = timetable;
        mViewLectureInfo   = lectureInfo;
        mViewLectureCancel = lectureCancel;
        mViewNews          = news;


        mButtonLectureInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.switchFragment(HomeActivity.VIEW_MODE.LECTURE_INFO);
            }
        });

        mButtonLectureCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.switchFragment(HomeActivity.VIEW_MODE.LECTURE_CANCEL);
            }
        });

        view.findViewById(R.id.button_news).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.switchFragment(HomeActivity.VIEW_MODE.NEWS);
            }
        });

        mTextViewLectureInfo = (TextView) view.findViewById(R.id.text_view_lecture_info);
        mTextViewLectureCancel = (TextView) view.findViewById(R.id.text_view_lecture_cancel);

        //Activity UI
        activity.setTitle(new CustomTitle(activity, activity.getString(R.string.title_dashboard)));
        ((NavigationView) activity.findViewById(R.id.nav_view)).getMenu().getItem(0).setChecked(true);
        activity.setViewMode(HomeActivity.VIEW_MODE.DASHBOARD);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        update();
    }

    public static DashboardFragment getInstance() {
        DashboardFragment fragment = new DashboardFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public void update() {
        List<MyClass> timetableList = PortalDataProvider.getTimetable();
        List<LectureInformation> lectureInfoList = PortalDataProvider.getLectureInfoList(LectureInformation.MY_CLASS);
        List<LectureCancellation> lectureCancelList = PortalDataProvider.getLectureCancelList(LectureCancellation.MY_CLASS);

        mMyTimetableRecyclerAdapter.updateDataList(timetableList);
        mLectureInfoAdapter.updateDataList(lectureInfoList);
        mLectureCancelAdapter.updateDataList(lectureCancelList);
        mNewsAdapter.updateDataList(PortalDataProvider.getNewsList());

        updateTimetableHeaderText();
        updateCardViewHeaderText(mHeaderLectureInfo  , mButtonLectureInfo  , getString(R.string.title_lecture_info_jp)  , lectureInfoList.size());
        updateCardViewHeaderText(mHeaderLectureCancel, mButtonLectureCancel, getString(R.string.title_lecture_cancel_jp), lectureCancelList.size());

        mTextViewLectureInfo.setVisibility((lectureInfoList.size() > 0) ? View.GONE : View.VISIBLE);
        mTextViewLectureCancel.setVisibility((lectureCancelList.size() > 0) ? View.GONE : View.VISIBLE);


        /*
        Expand or Collapse animation
         */
        refreshRecyclerViews();

        TransitionManager.beginDelayedTransition(mCardViewTimetable);
        TransitionManager.beginDelayedTransition(mCardViewLectureInfo);
        TransitionManager.beginDelayedTransition(mCardViewLectureCancel);
        TransitionManager.beginDelayedTransition(mCardViewNews);

        mCardViewTimetable.setVisibility((timetableList.size() > 0) ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView(RecyclerView view, RecyclerView.Adapter adapter){
        view.setLayoutManager(new LinearLayoutManager(getContext()));
        view.setAdapter(adapter);
        view.setNestedScrollingEnabled(false);
        view.setHasFixedSize(true);
    }

    private void refreshRecyclerViews(){
        mViewTimetable.setVisibility(View.GONE);
        mViewLectureInfo.setVisibility(View.GONE);
        mViewLectureCancel.setVisibility(View.GONE);
        mViewNews.setVisibility(View.GONE);

        mViewTimetable.setVisibility(View.VISIBLE);
        mViewLectureInfo.setVisibility(View.VISIBLE);
        mViewLectureCancel.setVisibility(View.VISIBLE);
        mViewNews.setVisibility(View.VISIBLE);
    }

    private void updateCardViewHeaderText(TextView view, TextView button, String title, final int infoSize) {
        StringBuilder classScheduleText = new StringBuilder(title);

        if (infoSize > 3) {
            classScheduleText.append(" （他");
            classScheduleText.append(infoSize - 3);
            classScheduleText.append("件）");

            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
        }

        view.setText(classScheduleText.toString());
    }

    private void updateTimetableHeaderText() {
        final TextView textView = (TextView) mCardViewTimetable.findViewById(R.id.header_timetable);
        final int dayOfWeek = PortalDataProvider.getTimetableWeek().ordinal();
        final String text = MyClass.DAY_OF_WEEK[dayOfWeek] + "曜日の時間割";

        textView.setText(text);
    }
}
