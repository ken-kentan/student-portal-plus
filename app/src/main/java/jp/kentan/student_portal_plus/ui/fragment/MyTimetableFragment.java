package jp.kentan.student_portal_plus.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import jp.kentan.student_portal_plus.ui.HomeActivity;
import jp.kentan.student_portal_plus.ui.MyClassEditActivity;
import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.ui.adapter.MyTimetableRecyclerAdapter;
import jp.kentan.student_portal_plus.ui.span.CustomTitle;


public class MyTimetableFragment extends Fragment {

    private final static int TYPE_LIST = 1;
    private final static int TYPE_WEEK = 2;

    private SharedPreferences mPreferences;

    private MyTimetableRecyclerAdapter mAdapter;

    private View mTimeTable, mListTimeTable;
    private TextView mTextViewMsg;
    private TextView mTextViewWeek[] = new TextView[5];

    private int mViewType = TYPE_WEEK;
    private int mMyClassInformationSize = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getContext();

        mPreferences = context.getSharedPreferences("common", Context.MODE_PRIVATE);

        mAdapter = new MyTimetableRecyclerAdapter(context, mViewType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        /*
        UI Initialize
         */
        final HomeActivity activity = (HomeActivity) getActivity();
        View v = inflater.inflate(R.layout.fragment_my_timetable, container, false);

        setHasOptionsMenu(true);

        RecyclerView recyclerViewWeek = (RecyclerView) v.findViewById(R.id.recyclerViewMyClassCard);
        RecyclerView recyclerViewDay  = (RecyclerView) v.findViewById(R.id.recyclerViewMyClassList);

        recyclerViewWeek.setLayoutManager(new GridLayoutManager(activity, 5 /* 月~金 */));
        recyclerViewWeek.setAdapter(mAdapter);
        recyclerViewWeek.setNestedScrollingEnabled(false);
        recyclerViewWeek.setHasFixedSize(true);

        recyclerViewDay.setLayoutManager(new LinearLayoutManager(activity));
        recyclerViewDay.setAdapter(mAdapter);
        recyclerViewDay.setHasFixedSize(true);

        mListTimeTable = recyclerViewDay;

        mTimeTable = v.findViewById(R.id.layout_timetable);
        mTextViewMsg = (TextView) v.findViewById(R.id.textViewMsg);

        mTextViewWeek[0] = (TextView) v.findViewById(R.id.text_mon);
        mTextViewWeek[1] = (TextView) v.findViewById(R.id.text_tue);
        mTextViewWeek[2] = (TextView) v.findViewById(R.id.text_wed);
        mTextViewWeek[3] = (TextView) v.findViewById(R.id.text_thu);
        mTextViewWeek[4] = (TextView) v.findViewById(R.id.text_fri);

        setViewType(mPreferences.getInt("timetable_view_type", TYPE_WEEK));

        //Activity UI
        activity.setTitle(new CustomTitle(activity, activity.getString(R.string.title_my_timetable)));
        ((NavigationView) activity.findViewById(R.id.nav_view)).getMenu().getItem(1).setChecked(true);

        activity.setViewMode(HomeActivity.VIEW_MODE.MY_CLASS);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_my_timetable_fragment, menu);

        menu.findItem(R.id.action_switch_view_type).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showViewSelectPopup();

                return false;
            }
        });
    }

    private void showViewSelectPopup() {
        final View view = getActivity().findViewById(R.id.action_switch_view_type);

        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenuInflater().inflate(R.menu.popup_menu_my_timetable_layout, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_view_week:
                        if (mViewType != TYPE_WEEK) setViewType(TYPE_WEEK);
                        break;
                    case R.id.action_view_day:
                        if (mViewType != TYPE_LIST){
                            setViewType(TYPE_LIST);
                        }
                        break;
                }
                return true;
            }
        });

        popup.show();

        MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popup.getMenu(), view);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        update();
    }

    public static MyTimetableFragment getInstance() {
        return new MyTimetableFragment();
    }

    public void update() {
        List<MyClass> list = PortalDataProvider.getMyClassList();
        mMyClassInformationSize = list.size();

        setVisibility(mMyClassInformationSize > 0);

        final int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2;
        for(int i=0; i<5; ++i){
            mTextViewWeek[i].setTypeface(null, (i == dayOfWeek) ? Typeface.BOLD : Typeface.NORMAL);
        }

        mAdapter.updateDataList(list);
    }

    private void setViewType(final int viewType) {
        mViewType = viewType;

        setVisibility(mMyClassInformationSize > 0);

        mAdapter.setViewType(mViewType);
        mAdapter.notifyDataSetChanged();

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("timetable_view_type", mViewType);
        editor.apply();
    }

    private void setVisibility(boolean isVisible){
        if (isVisible) {
            mTextViewMsg.setVisibility(View.GONE);
            mTimeTable.setVisibility(    (mViewType == TYPE_WEEK) ? View.VISIBLE : View.GONE);
            mListTimeTable.setVisibility((mViewType == TYPE_LIST) ? View.VISIBLE : View.GONE);
        } else {
            mTextViewMsg.setVisibility(View.VISIBLE);
            mTimeTable.setVisibility(View.GONE);
            mListTimeTable.setVisibility(View.GONE);
        }
    }
}
