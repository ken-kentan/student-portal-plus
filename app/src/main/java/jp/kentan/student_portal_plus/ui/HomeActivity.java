package jp.kentan.student_portal_plus.ui;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.data.shibboleth.ShibbolethData;
import jp.kentan.student_portal_plus.ui.fragment.*;
import jp.kentan.student_portal_plus.notification.NotificationController;
import jp.kentan.student_portal_plus.notification.NotificationScheduler;
import jp.kentan.student_portal_plus.ui.widget.MapViewer;
import jp.kentan.student_portal_plus.ui.widget.ScrollAwareFabBehavior;
import jp.kentan.student_portal_plus.util.StringUtils;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Animator.AnimatorListener, PortalDataProvider.Callback {

    public enum VIEW_MODE {NONE, DASHBOARD, MY_CLASS, LECTURE_INFO, LECTURE_CANCEL, NEWS}

    private VIEW_MODE mViewMode = VIEW_MODE.NONE;

    private DashboardFragment mDashboardFragment;
    private MyTimetableFragment mMyTimetableFragment;
    private LectureInformationFragment mLectureInformationFragment;
    private LectureCancellationFragment mLectureCancellationFragment;
    private NewsFragment mNewsFragment;

    private NavigationView mNavigationView;
    private DrawerLayout mDrawer;
    private FloatingActionButton mFabRefresh;

    private Snackbar mSnackbarFinish;

    private PortalDataProvider mPortalData;

    private SharedPreferences mPreferenceCommon;

    private boolean isShowOptionsMenu = false;
    private boolean isReadyExit = false;

    private final FloatingActionButton.OnVisibilityChangedListener FAB_SHOWN_LISTENER = new FloatingActionButton.OnVisibilityChangedListener() {
        @Override
        public void onShown(FloatingActionButton fab) {
            super.onShown(fab);

            if(PortalDataProvider.isFetching()){
                animateFabRefresh(fab);
            }else{
                fab.setRotation(0.0f);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Common Initialization
         */
        mPreferenceCommon = getSharedPreferences("common", MODE_PRIVATE);


        /*
        Data Initialization
         */
        mPortalData = new PortalDataProvider(this, this);


        /*
        Layout Initialization
         */
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        mNavigationView.setNavigationItemSelectedListener(this);

        mFabRefresh = (FloatingActionButton) findViewById(R.id.fab_refresh);
        mFabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!PortalDataProvider.isFetching()){
                    mPortalData.fetch();

                    animateFabRefresh();
                }
            }
        });
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mFabRefresh.getLayoutParams();
        params.setBehavior(new ScrollAwareFabBehavior(FAB_SHOWN_LISTENER));
        mFabRefresh.setLayoutParams(params);


        /*
        Fragment Initialization
         */
        mDashboardFragment           = DashboardFragment.getInstance();
        mMyTimetableFragment         = MyTimetableFragment.getInstance();
        mLectureInformationFragment  = LectureInformationFragment.getInstance();
        mLectureCancellationFragment = LectureCancellationFragment.getInstance();
        mNewsFragment                = NewsFragment.getInstance();

        // Instance state
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.container, mDashboardFragment);
            transaction.commit();
        } else {
            clearFragmentBackStack();
            switchFragment(VIEW_MODE.values()[savedInstanceState.getInt("view_mode")]);
        }


        /*
        UI Initialization
         */
        updateAccountHeader();
        new NotificationController(this).cancelAll();
        if(PortalDataProvider.isFetching()){
            animateFabRefresh();
        }


        /*
        Welcome Activity
         */
        if (mPreferenceCommon.getBoolean("first_time", true)) {
            Intent welcome = new Intent(this, WelcomeActivity.class);
            startActivity(welcome);
            finish();
        } else {
            Intent intent = getIntent();
            final int viewMode = intent.getIntExtra("view_mode", -1);

            if (intent.getBooleanExtra("first_login", false)) {
                mPortalData.fetch();
                animateFabRefresh();
            } else if (viewMode >= 0) {
                switchFragment(VIEW_MODE.values()[viewMode]);
            }

            new NotificationScheduler(this).scheduleIfNeed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("view_mode", mViewMode.ordinal());
    }


    /*
    UI methods
     */

    //Fragment切り替え
    public void switchFragment(VIEW_MODE mode) {
        if (mode == mViewMode) return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);

        switch (mode) {
            case DASHBOARD:
                transaction.replace(R.id.container, mDashboardFragment);
                break;
            case MY_CLASS:
                transaction.replace(R.id.container, mMyTimetableFragment);
                break;
            case LECTURE_INFO:
                transaction.replace(R.id.container, mLectureInformationFragment);
                break;
            case LECTURE_CANCEL:
                transaction.replace(R.id.container, mLectureCancellationFragment);
                break;
            case NEWS:
                transaction.replace(R.id.container, mNewsFragment);
                break;
        }

        mFabRefresh.show(FAB_SHOWN_LISTENER);
        transaction.addToBackStack(null);
        transaction.commit();
        mViewMode = mode;
    }

    //FABをぐーるぐーる回す
    public void animateFabRefresh(FloatingActionButton fab){
        fab.setRotation(0.0f);
        fab.animate().rotation(360.0f).withLayer().setDuration(1000).setListener(this).start();
    }

    public void animateFabRefresh(){
        animateFabRefresh(mFabRefresh);
    }

    //NavigationViewのAccountHeaderを更新
    private void updateAccountHeader() {
        final View v = mNavigationView.getHeaderView(0);

        final ShibbolethData data = new ShibbolethData(this);
        final String name     = data.getName();
        final String username = data.getUsername();

        boolean isAvailable = !StringUtils.isEmpty(name) && !StringUtils.isEmpty(username);

        if (isAvailable) {
            ((TextView) v.findViewById(R.id.textViewName)).setText(name);
            ((TextView) v.findViewById(R.id.textViewUsername)).setText(username);
        }

        (v.findViewById(R.id.imageViewAccount)).setVisibility((isAvailable) ? View.VISIBLE : View.INVISIBLE);
        (v.findViewById(R.id.textViewName)).setVisibility(    (isAvailable) ? View.VISIBLE : View.INVISIBLE);
        (v.findViewById(R.id.textViewUsername)).setVisibility((isAvailable) ? View.VISIBLE : View.INVISIBLE);
    }

    //各Fragmentからcall. OptionsMenuなどのレイアウトを更新
    public void setViewMode(VIEW_MODE mode) {
        mViewMode = mode;

        isShowOptionsMenu = (mode != VIEW_MODE.DASHBOARD);
        supportInvalidateOptionsMenu();
    }


    /*
    OnNavigationItemSelectedListener
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        mDrawer.closeDrawer(GravityCompat.START);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (item.getItemId()) {
                    case R.id.nav_dashboard:
                        switchFragment(VIEW_MODE.DASHBOARD);
                        break;
                    case R.id.nav_my_timetable:
                        switchFragment(VIEW_MODE.MY_CLASS);
                        break;
                    case R.id.nav_class_schedule_change:
                        switchFragment(VIEW_MODE.LECTURE_INFO);
                        break;
                    case R.id.nav_class_cancel:
                        switchFragment(VIEW_MODE.LECTURE_CANCEL);
                        break;
                    case R.id.nav_news_events:
                        switchFragment(VIEW_MODE.NEWS);
                        break;
                    case R.id.nav_campus_map:
                        MapViewer.show(getApplicationContext(), MapViewer.CAMPUS_MAP);
                        break;
                    case R.id.nav_room_map:
                        MapViewer.show(getApplicationContext(), MapViewer.ROOM_MAP);
                        break;
                    case R.id.nav_setting:
                        Intent setting = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(setting);
                        break;
                }
            }
        }, 300); //切り替えをスムーズに見せるために300ms遅延させる

        return true;
    }

    /*
    OptionsMenu Listener
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return isShowOptionsMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());

        if(mViewMode == VIEW_MODE.MY_CLASS){
            mMyTimetableFragment.onMenuItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }


    /*
    AnimatorListener (FAB)
     */
    @Override
    public void onAnimationStart(Animator animator) {}

    @Override
    public void onAnimationEnd(Animator animator) {
        if (PortalDataProvider.isFetching()) {
            animateFabRefresh();
        }
    }

    @Override
    public void onAnimationCancel(Animator animator) {}

    @Override
    public void onAnimationRepeat(Animator animator) {}


    /*
    PortalDataProvider Callback
     */
    @Override
    public void failed(String errorMessage, Throwable error) {
        if (error != null && !mPreferenceCommon.getBoolean("detail_err_msg", false)) {
            errorMessage = getString(R.string.err_msg_failed_to_update);
        }

        final Snackbar snackbar = Snackbar.make(mFabRefresh, errorMessage, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("閉じる", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        }).show();
    }

    @Override
    public void success() {
        switch (mViewMode) {
            case DASHBOARD:
                mDashboardFragment.update();
                break;
            case MY_CLASS:
                mMyTimetableFragment.update();
                break;
            case LECTURE_INFO:
                mLectureInformationFragment.update();
                break;
            case LECTURE_CANCEL:
                mLectureCancellationFragment.update();
                break;
            case NEWS:
                mNewsFragment.update();
                break;
        }

        Snackbar.make(mFabRefresh, "更新しました", Snackbar.LENGTH_SHORT).show();
    }


    /*
    Other
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (getSupportFragmentManager().getBackStackEntryCount() > 0 && mViewMode != VIEW_MODE.DASHBOARD) {
                getSupportFragmentManager().popBackStack();
                mFabRefresh.show(FAB_SHOWN_LISTENER);
                return;
            } else if (!isReadyExit) {
                isReadyExit = true;
                mSnackbarFinish = Snackbar.make(mFabRefresh, getString(R.string.msg_back_to_exit), Snackbar.LENGTH_LONG);
                mSnackbarFinish.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        isReadyExit = false;
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                    }
                }).show();

                return;
            }

            if(mSnackbarFinish != null){
                mSnackbarFinish.dismiss();
                mSnackbarFinish = null;
            }
            this.moveTaskToBack(true);
        }
    }

    private void clearFragmentBackStack(){
        final FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }
}
