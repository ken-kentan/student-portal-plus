package jp.kentan.student_portal_plus.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import jp.kentan.student_portal_plus.ui.HomeActivity;
import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.data.component.LectureInformation;
import jp.kentan.student_portal_plus.ui.adapter.LectureInformationRecyclerAdapter;
import jp.kentan.student_portal_plus.ui.span.CustomTitle;
import jp.kentan.student_portal_plus.util.AnimationUtils;
import jp.kentan.student_portal_plus.util.StringUtils;


public class LectureInformationFragment extends Fragment implements SearchView.OnQueryTextListener {

    private static Bundle sState = null;

    private SharedPreferences mPreference;

    private LectureInformationRecyclerAdapter mAdapter;

    private TextView mTextView;

    private String mSearchText = "";
    private int mSortBy = 0;
    private boolean isShowUnread = true, isShowRead = true, isShowMyClass = true;

    private Spinner mSpinnerSortBy;
    private CheckBox mCheckBoxUnread, mCheckBoxRead, mCheckBoxMyClass;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getContext();

        mPreference = context.getSharedPreferences("common", Context.MODE_PRIVATE);

        mAdapter = new LectureInformationRecyclerAdapter(context, 1);

        if (savedInstanceState != null || (savedInstanceState = getArguments()) != null) {
            restore(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        /*
        UI Initialize
         */
        final HomeActivity activity = (HomeActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_lecture_info, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);

        mTextView = view.findViewById(R.id.text_view);

        //Activity UI
        activity.setTitle(new CustomTitle(activity, activity.getString(R.string.title_class_schedule)));
        ((NavigationView) activity.findViewById(R.id.nav_view)).getMenu().getItem(2).setChecked(true);
        activity.setViewMode(HomeActivity.VIEW_MODE.LECTURE_INFO);


        mSortBy = mPreference.getInt("lecture_info_sort_by", 0);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_lecture_info_fragment, menu);

        /*
        Search view initialize
         */
        final MenuItem searchItem = menu.findItem(R.id.search_view);

        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchText = "";
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }
        });
        searchView.setQueryHint(getString(R.string.menu_search_with_subject_or_instructor));

        final String query = mSearchText;
        if (!TextUtils.isEmpty(query)) {
            searchItem.expandActionView();
            searchView.setQuery(query, true);
            searchView.clearFocus();
        }


        menu.findItem(R.id.action_list_filter).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

                dialog.setView(buildFilterDialog());
                dialog.setTitle(getString(R.string.title_search_filter));
                dialog.setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mSortBy = (int) mSpinnerSortBy.getSelectedItemId();

                        isShowUnread = mCheckBoxUnread.isChecked();
                        isShowRead = mCheckBoxRead.isChecked();
                        isShowMyClass = mCheckBoxMyClass.isChecked();

                        SharedPreferences.Editor editor = mPreference.edit();
                        editor.putInt("lecture_info_sort_by", mSortBy);
                        editor.apply();

                        searchView.clearFocus();
                        update();
                    }
                });
                dialog.setNegativeButton(getString(R.string.cancel), null);
                dialog.create().show();

                return false;
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchText = newText;
        update();
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        putState(outState);
        sState = outState;

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        if(sState == null) sState = new Bundle();

        putState(sState);
    }

    @Override
    public void onResume() {
        super.onResume();

        update();
    }

    public static LectureInformationFragment getInstance() {
        LectureInformationFragment fragment = new LectureInformationFragment();
        if(sState != null){
            fragment.setArguments(sState);
        }

        return fragment;
    }

    public void update() {
        List<LectureInformation> list = PortalDataProvider.getLectureInfoList(StringUtils.splitWithSpace(mSearchText), mSortBy, isShowUnread, isShowRead, isShowMyClass);
        mAdapter.updateDataList(list);

        if(list.size() > 0){
            if(mTextView.getVisibility() == View.VISIBLE) mTextView.startAnimation(AnimationUtils.fadeOut(mTextView));
        }else{
            mTextView.setText(getString((mSearchText.length() > 0 || !isShowUnread || !isShowRead || !isShowMyClass) ? R.string.msg_no_result_lecture_info : R.string.msg_no_lecture_info));
            if(mTextView.getVisibility() == View.GONE) mTextView.startAnimation(AnimationUtils.fadeIn(mTextView));
        }
    }

    private void restore(Bundle state) {
        mSearchText   = state.getString("SEARCH_TEXT");
        mSortBy       = state.getInt("SORT_BY");
        isShowUnread  = state.getBoolean("IS_SHOW_UNREAD");
        isShowRead    = state.getBoolean("IS_SHOW_READ");
        isShowMyClass = state.getBoolean("IS_SHOW_MY_CLASS");
    }

    private View buildFilterDialog() {
        final Context context = getContext();

        final int COLOR_CHECKED   = ContextCompat.getColor(context, R.color.chip_checked_text);
        final int COLOR_UNCHECKED = ContextCompat.getColor(context, R.color.chip_unchecked_text);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setTextColor((isChecked) ? COLOR_CHECKED : COLOR_UNCHECKED);
            }
        };


        final View view = View.inflate(context, R.layout.dialog_filter_lecture_info, null);

        mSpinnerSortBy   = view.findViewById(R.id.sort_by);
        mCheckBoxUnread  = view.findViewById(R.id.unread);
        mCheckBoxRead    = view.findViewById(R.id.read);
        mCheckBoxMyClass = view.findViewById(R.id.my_class);

        mSpinnerSortBy.setSelection(mSortBy);

        mCheckBoxUnread.setOnCheckedChangeListener(listener);
        mCheckBoxRead.setOnCheckedChangeListener(listener);
        mCheckBoxMyClass.setOnCheckedChangeListener(listener);

        mCheckBoxUnread.setChecked(isShowUnread);
        mCheckBoxRead.setChecked(isShowRead);
        mCheckBoxMyClass.setChecked(isShowMyClass);

        return view;
    }

    private void putState(Bundle state){
        state.putBoolean("IS_SHOW_UNREAD", isShowUnread);
        state.putBoolean("IS_SHOW_READ", isShowRead);
        state.putBoolean("IS_SHOW_MY_CLASS", isShowMyClass);

        state.putInt("SORT_BY", mSortBy);

        state.putString("SEARCH_TEXT", mSearchText);
    }
}
