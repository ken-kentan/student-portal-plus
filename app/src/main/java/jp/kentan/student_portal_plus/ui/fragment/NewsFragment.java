package jp.kentan.student_portal_plus.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
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
import jp.kentan.student_portal_plus.data.component.News;
import jp.kentan.student_portal_plus.ui.adapter.NewsRecyclerAdapter;
import jp.kentan.student_portal_plus.ui.span.CustomTitle;
import jp.kentan.student_portal_plus.util.StringUtils;


public class NewsFragment extends Fragment implements SearchView.OnQueryTextListener {

    private static Bundle sState;

    private NewsRecyclerAdapter mAdapter;

    private TextView mTextViewMsg;

    private String mSearchText = "";
    private int periodPostingDate = 0; //(0:All, 1:Today, 2:ThisWeek, 3:ThisMonth, 4:ThisYear)
    private boolean isShowUnread = true, isShowRead = true, isShowFavorite = true;

    private Spinner mSpinnerPostingDate;
    private CheckBox mCheckBoxUnread, mCheckBoxRead, mCheckBoxFavorite;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getContext();

        mAdapter = new NewsRecyclerAdapter(context, null, 1);

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
        final View view = inflater.inflate(R.layout.fragment_news, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewLatestInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);

        mTextViewMsg = (TextView) view.findViewById(R.id.textViewMsg);

        //Activity UI
        activity.setTitle(new CustomTitle(activity, activity.getString(R.string.title_news_and_events)));
        ((NavigationView) activity.findViewById(R.id.nav_view)).getMenu().getItem(4).setChecked(true);
        activity.setViewMode(HomeActivity.VIEW_MODE.NEWS);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_news_fragment, menu);

        /*
        Search view initialize
         */
        final MenuItem searchItem = menu.findItem(R.id.search_view);

        final SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
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
        searchView.setQueryHint(getString(R.string.menu_search_with_title));

        final String query = mSearchText;
        if (!TextUtils.isEmpty(query)) {
            searchItem.expandActionView();
            searchView.setQuery(query, true);
            searchView.clearFocus();
        }

        menu.findItem(R.id.action_list_filter).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

                dialogBuilder.setView(buildFilterDialog());
                dialogBuilder.setTitle(getString(R.string.title_search_filter));
                dialogBuilder.setPositiveButton(getString(R.string.apply), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        periodPostingDate = (int) mSpinnerPostingDate.getSelectedItemId();

                        isShowUnread = mCheckBoxUnread.isChecked();
                        isShowRead = mCheckBoxRead.isChecked();
                        isShowFavorite = mCheckBoxFavorite.isChecked();

                        searchView.clearFocus();
                        update();
                    }
                });
                dialogBuilder.setNegativeButton(getString(R.string.cancel), null);
                dialogBuilder.create().show();

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

    public static NewsFragment getInstance() {
        NewsFragment fragment = new NewsFragment();
        if(sState != null){
            fragment.setArguments(sState);
        }

        return fragment;
    }

    public void update() {
        List<News> list = PortalDataProvider.getNewsList(StringUtils.splitWithSpace(mSearchText), periodPostingDate, isShowUnread, isShowRead, isShowFavorite);

        mAdapter.updateDataList(list);

        mTextViewMsg.setVisibility((list.size() > 0) ? View.GONE : View.VISIBLE);
    }

    private void restore(Bundle state) {
        mSearchText       = state.getString("SEARCH_TEXT");
        periodPostingDate = state.getInt("POSTING_DATE_PERIOD");
        isShowUnread      = state.getBoolean("IS_SHOW_UNREAD");
        isShowRead        = state.getBoolean("IS_SHOW_READ");
        isShowFavorite    = state.getBoolean("IS_SHOW_FAVORITE");
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


        final View view = View.inflate(context, R.layout.dialog_filter_latest_info, null);

        mSpinnerPostingDate = (Spinner) view.findViewById(R.id.posting_date);

        mCheckBoxUnread   = (CheckBox) view.findViewById(R.id.unread);
        mCheckBoxRead     = (CheckBox) view.findViewById(R.id.read);
        mCheckBoxFavorite = (CheckBox) view.findViewById(R.id.icon);

        mSpinnerPostingDate.setSelection(periodPostingDate);

        mCheckBoxUnread.setOnCheckedChangeListener(listener);
        mCheckBoxRead.setOnCheckedChangeListener(listener);
        mCheckBoxFavorite.setOnCheckedChangeListener(listener);

        mCheckBoxUnread.setChecked(isShowUnread);
        mCheckBoxRead.setChecked(isShowRead);
        mCheckBoxFavorite.setChecked(isShowFavorite);

        return view;
    }

    private void putState(Bundle state){
        state.putBoolean("IS_SHOW_UNREAD", isShowUnread);
        state.putBoolean("IS_SHOW_READ", isShowRead);
        state.putBoolean("IS_SHOW_FAVORITE", isShowFavorite);

        state.putInt("POSTING_DATE_PERIOD", periodPostingDate);

        state.putString("SEARCH_TEXT", mSearchText);
    }
}
