package jp.kentan.student_portal_plus.ui;


import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.DatabaseProvider;
import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.data.component.LectureCancellation;
import jp.kentan.student_portal_plus.data.component.LectureInformation;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.data.component.News;
import jp.kentan.student_portal_plus.notification.NotificationScheduler;
import jp.kentan.student_portal_plus.notification.NotificationService;
import jp.kentan.student_portal_plus.ui.widget.MyClassSamplePreference;

public class SettingsActivity extends AppCompatActivity {

    private static SettingsActivity sActivity;

    private static SharedPreferences sPreferenceCommon, sPreferenceNotification;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_settings));

        /*
        Fragment Initialize
         */
        FragmentManager fm = getFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferencesFragment()).commit();
        setupActionBar();

        sActivity = this;

        sPreferenceCommon = sActivity.getSharedPreferences("common", MODE_PRIVATE);
        sPreferenceNotification = sActivity.getSharedPreferences("notification", MODE_PRIVATE);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        final int stackCnt = getFragmentManager().getBackStackEntryCount();
        if (stackCnt > 0) {
            getFragmentManager().popBackStack();

            if (stackCnt <= 1) {
                setTitle(getString(R.string.title_activity_settings));
            }
            return;
        }

        super.onBackPressed();
    }


    public static class PreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

        private Preference mNotifyContents;
        private ListPreference mAutoFetchInterval, mLimitOfLatestInfo;
        private CheckBoxPreference mNotifyWithVibrate, mNotifyWithLed;

        private NotificationScheduler mNotificationScheduler;

        private static Toast sToast;
        private int countVersionClick = 5;
        private final static String[] SECRET_TEXT = {"nyan~ > \uD83D\uDC31", "(´・ω・｀)", "have a nice day \uD83C\uDF08", "thanks <3"};


        @Override
        public void onCreate(Bundle saveInstanceState) {
            super.onCreate(saveInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            mNotificationScheduler = new NotificationScheduler(getActivity());


            /*
            Set default values
             */
            mNotifyContents = getPreferenceScreen().findPreference("pref_key_notify_contents");
            mAutoFetchInterval = (ListPreference) getPreferenceScreen().findPreference("pref_key_auto_fetch_interval");
            mLimitOfLatestInfo = (ListPreference) getPreferenceScreen().findPreference("pref_key_limit_of_latest_info");
            mNotifyWithVibrate = (CheckBoxPreference) getPreferenceScreen().findPreference("pref_key_notify_with_vibrate");
            mNotifyWithLed = (CheckBoxPreference) getPreferenceScreen().findPreference("pref_key_notify_with_led");

            final boolean isAutoFetch = sPreferenceCommon.getBoolean("auto_fetch", true);
            mNotifyContents.setEnabled(isAutoFetch);
            mAutoFetchInterval.setEnabled(isAutoFetch);
            mNotifyWithVibrate.setEnabled(isAutoFetch);
            mNotifyWithLed.setEnabled(isAutoFetch);


            /*
            Set EventListener
             */
            mNotifyContents.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction().replace(android.R.id.content, new NotifyContentsPreferencesFragment()).addToBackStack(null).commit();
                    return true;
                }
            });

            getPreferenceScreen().findPreference("pref_key_select_update_data").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction().replace(android.R.id.content, new SelectUpdateContentsPreferencesFragment()).addToBackStack(null).commit();
                    return true;
                }
            });
            getPreferenceScreen().findPreference("pref_key_my_class_threshold").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction().replace(android.R.id.content, new MyClassThresholdPreferencesFragment()).addToBackStack(null).commit();
                    return true;
                }
            });

            getPreferenceScreen().findPreference("pref_key_delete_all_data").setOnPreferenceClickListener(this);
            getPreferenceScreen().findPreference("pref_key_terms").setOnPreferenceClickListener(this);
            getPreferenceScreen().findPreference("pref_key_oss_license").setOnPreferenceClickListener(this);
            getPreferenceScreen().findPreference("pref_key_version").setOnPreferenceClickListener(this);


            updatePrefSummary();
        }

        private void updatePrefSummary() {
            updateLastLoginDate();

            final int interval = sPreferenceCommon.getInt("auto_fetch_interval_m", 60);
            String autoFetchSummary;

            if (interval >= 60) {
                autoFetchSummary = Integer.toString(interval / 60) + "時間毎に更新する";
            } else {
                autoFetchSummary = Integer.toString(interval) + "分毎に更新する";
            }

            mAutoFetchInterval.setSummary(autoFetchSummary);
            mLimitOfLatestInfo.setSummary(sPreferenceCommon.getInt("limit_of_latest_info", 100) + "件 (古い情報は自動的に消去されます)");
        }

        private void updateLastLoginDate(){
            String strLastLoginDate = sActivity.getSharedPreferences("IDP", Context.MODE_PRIVATE).getString("last_login_date", "未ログイン");
            getPreferenceScreen().findPreference("pref_key_idp_last_login_date").setSummary(strLastLoginDate);
        }

        @Override
        public boolean onPreferenceClick(Preference pref) {
            switch (pref.getKey()) {
                case "pref_key_delete_all_data":
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setIcon(R.drawable.ic_warning);
                    builder.setTitle("ポータルデータ消去");
                    builder.setMessage(getString(R.string.msg_warn_reset));
                    builder.setNegativeButton("いいえ", null);

                    builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(getActivity(), DatabaseProvider.deleteAll() + "件のデータを消去しました", Toast.LENGTH_LONG).show();
                        }

                    });

                    builder.show();
                    break;
                case "pref_key_terms":
                    AlertDialog.Builder dialogTerms = new AlertDialog.Builder(sActivity);
                    dialogTerms.setTitle(getString(R.string.pref_title_terms));

                    final WebView webTerms = new WebView(sActivity);
                    webTerms.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                    webTerms.getSettings().setAppCacheEnabled(false);
                    webTerms.loadUrl(getString(R.string.url_terms));
                    webTerms.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            //オンライン取得に失敗
                            if (!webTerms.getTitle().contains(getString(R.string.title_terms))) {
                                webTerms.loadUrl(getString(R.string.url_terms_local));
                            }
                        }
                    });
                    dialogTerms.setView(webTerms);
                    dialogTerms.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    dialogTerms.show();
                    break;
                case "pref_key_oss_license":
                    AlertDialog.Builder dialogOssLicenses = new AlertDialog.Builder(sActivity);
                    dialogOssLicenses.setTitle(getString(R.string.pref_title_oss_license));

                    WebView webOssLicenses = new WebView(sActivity);
                    webOssLicenses.loadUrl("file:///android_asset/licenses.html");
                    webOssLicenses.setWebViewClient(new WebViewClient());
                    dialogOssLicenses.setView(webOssLicenses);
                    dialogOssLicenses.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    dialogOssLicenses.show();
                    break;
                case "pref_key_version":
                    final String msg;
                    if(countVersionClick < 1){
                        countVersionClick = 5;
                        msg = SECRET_TEXT[(int)(Math.random() * SECRET_TEXT.length)];
                    }else{
                        msg = Integer.toString(countVersionClick--);
                    }

                    if(sToast != null) sToast.cancel();

                    sToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
                    sToast.show();
                    break;
            }
            return true;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
            final SharedPreferences.Editor editorCommon = sPreferenceCommon.edit();
            final SharedPreferences.Editor editorNotification = sPreferenceNotification.edit();

            switch (key) {
                case "pref_key_auto_fetch":
                    boolean isFetch = pref.getBoolean(key, true);

                    editorCommon.putBoolean("auto_fetch", isFetch);
                    mNotifyContents.setEnabled(isFetch);
                    mAutoFetchInterval.setEnabled(isFetch);
                    mNotifyWithVibrate.setEnabled(isFetch);
                    mNotifyWithLed.setEnabled(isFetch);

                    if (isFetch) {
                        mNotificationScheduler.schedule();
                    } else {
                        mNotificationScheduler.cancel();
                    }
                    break;
                case "pref_key_auto_fetch_interval":
                    final int interval = Integer.parseInt(mAutoFetchInterval.getValue());
                    String autoFetchSummary;

                    if (interval >= 60) {
                        autoFetchSummary = Integer.toString(interval / 60) + "時間毎に更新する";
                    } else {
                        autoFetchSummary = Integer.toString(interval) + "分毎に更新する";
                    }

                    editorCommon.putInt("auto_fetch_interval_m", interval);
                    mAutoFetchInterval.setSummary(autoFetchSummary);

                    mNotificationScheduler.schedule(interval);
                    break;
                case "pref_key_notify_with_vibrate":
                    editorNotification.putBoolean("vibrate", pref.getBoolean(key, true));
                    break;
                case "pref_key_notify_with_led":
                    editorNotification.putBoolean("led", pref.getBoolean(key, true));
                    break;
                case "pref_key_limit_of_latest_info":
                    String limit = pref.getString(key, "90");

                    editorCommon.putInt("limit_of_latest_info", Integer.parseInt(limit));
                    mLimitOfLatestInfo.setSummary(limit + "件 (古い情報は自動的に消去されます)");
                    break;
                case "pref_key_pdf_open_with_gdocs":
                    editorCommon.putBoolean("pdf_open_with_gdocs", pref.getBoolean(key, true));
                    break;
                case "pref_key_detail_err_msg":
                    editorCommon.putBoolean("detail_err_msg", pref.getBoolean(key, false));
                    break;
            }

            editorCommon.apply();
            editorNotification.apply();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            updateLastLoginDate();
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }


    public static class NotifyContentsPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private final static String[] NOTIFY_TYPE = {"通知しない", "すべて", "受講科目・類似科目"};

        private ListPreference mNotifyLectureInfo, mNotifyCancelInfo, mNotifyLatestInfo;

        @Override
        public void onCreate(Bundle saveInstanceState) {
            super.onCreate(saveInstanceState);
            addPreferencesFromResource(R.xml.pref_notify_contents);

            mNotifyLectureInfo = (ListPreference) findPreference("pref_key_notify_type_lecture_info");
            mNotifyCancelInfo = (ListPreference) findPreference("pref_key_notify_type_cancel_info");
            mNotifyLatestInfo = (ListPreference) findPreference("pref_key_notify_type_latest_info");

            getActivity().setTitle("新着通知");

            updatePrefSummary();
        }

        private void updatePrefSummary() {
            mNotifyLectureInfo.setSummary(NOTIFY_TYPE[sPreferenceNotification.getInt("type_lecture_info", NotificationService.NOTIFY_ALL) + 1]);
            mNotifyCancelInfo.setSummary( NOTIFY_TYPE[sPreferenceNotification.getInt("type_cancel_info", NotificationService.NOTIFY_ALL) + 1]);
            mNotifyLatestInfo.setSummary( NOTIFY_TYPE[sPreferenceNotification.getInt("type_latest_info", NotificationService.NOTIFY_ALL) + 1]);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
            final SharedPreferences.Editor editor = sPreferenceNotification.edit();
            final int index = Integer.parseInt(pref.getString(key, ""));

            switch (key) {
                case "pref_key_notify_type_lecture_info":

                    mNotifyLectureInfo.setSummary(NOTIFY_TYPE[index + 1]);
                    editor.putInt("type_lecture_info", index);
                    break;
                case "pref_key_notify_type_cancel_info":

                    mNotifyCancelInfo.setSummary(NOTIFY_TYPE[index + 1]);
                    editor.putInt("type_cancel_info", index);
                    break;
                case "pref_key_notify_type_latest_info":

                    mNotifyLatestInfo.setSummary(NOTIFY_TYPE[index + 1]);
                    editor.putInt("type_latest_info", index);
                    break;
            }

            editor.apply();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }


    public static class SelectUpdateContentsPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private final static String LAST_DATE_HEAD = "最終更新: ";

        private SharedPreferences mPreferences;

        private CheckBoxPreference mMyClass, mLectureInfo, mCancelInfo, mLatestInfo;

        @Override
        public void onCreate(Bundle saveInstanceState) {
            super.onCreate(saveInstanceState);
            addPreferencesFromResource(R.xml.pref_select_update_contents);

            mPreferences = getActivity().getSharedPreferences("update_content", MODE_PRIVATE);

            getActivity().setTitle("ポータルデータ");

            SwitchPreference mAll = (SwitchPreference) findPreference("pref_key_update_all");
            mMyClass = (CheckBoxPreference) findPreference("pref_key_update_my_class");
            mLectureInfo = (CheckBoxPreference) findPreference("pref_key_update_lecture_info");
            mCancelInfo = (CheckBoxPreference) findPreference("pref_key_update_cancel_info");
            mLatestInfo = (CheckBoxPreference) findPreference("pref_key_update_latest_info");

            mMyClass.setChecked(mPreferences.getBoolean(MyClass.KEY, true));
            mLectureInfo.setChecked(mPreferences.getBoolean(LectureInformation.KEY, true));
            mCancelInfo.setChecked(mPreferences.getBoolean(LectureCancellation.KEY, true));
            mLatestInfo.setChecked(mPreferences.getBoolean(News.KEY, true));

            if (mPreferences.getBoolean("all", true)) {
                mAll.setChecked(true);
                setEnabledAllCheckBox(false);
            } else {
                mAll.setChecked(false);
            }

            updatePrefSummary();
        }

        private void updatePrefSummary() {
            mMyClass.setSummary(    LAST_DATE_HEAD + mPreferences.getString(MyClass.KEY_DATE, "未取得"));
            mLectureInfo.setSummary(LAST_DATE_HEAD + mPreferences.getString(LectureInformation.KEY_DATE, "未取得"));
            mCancelInfo.setSummary( LAST_DATE_HEAD + mPreferences.getString(LectureCancellation.KEY_DATE, "未取得"));
            mLatestInfo.setSummary( LAST_DATE_HEAD + mPreferences.getString(News.KEY_DATE, "未取得"));
        }

        private void setEnabledAllCheckBox(boolean enabled) {
            mMyClass.setEnabled(enabled);
            mLectureInfo.setEnabled(enabled);
            mCancelInfo.setEnabled(enabled);
            mLatestInfo.setEnabled(enabled);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
            final SharedPreferences.Editor editor = mPreferences.edit();

            switch (key) {
                case "pref_key_update_all":
                    final boolean isAll = pref.getBoolean(key, true);

                    editor.putBoolean("all", isAll);
                    setEnabledAllCheckBox(!isAll);
                    break;
                case "pref_key_update_my_class":
                    editor.putBoolean(MyClass.KEY, pref.getBoolean(key, true));
                    break;
                case "pref_key_update_lecture_info":
                    editor.putBoolean(LectureInformation.KEY, pref.getBoolean(key, true));
                    break;
                case "pref_key_update_cancel_info":
                    editor.putBoolean(LectureCancellation.KEY, pref.getBoolean(key, true));
                    break;
                case "pref_key_update_latest_info":
                    editor.putBoolean(News.KEY, pref.getBoolean(key, true));
                    break;
            }

            editor.apply();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }


    public static class MyClassThresholdPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private ListPreference mMyClassThreshold;
        private MyClassSamplePreference mMyClassSample;


        @Override
        public void onCreate(Bundle saveInstanceState) {
            super.onCreate(saveInstanceState);
            addPreferencesFromResource(R.xml.pref_my_class_threshold);

            getActivity().setTitle("ポータルデータ");

            mMyClassThreshold = (ListPreference)findPreference("pref_key_my_class_threshold");
            mMyClassSample = (MyClassSamplePreference)findPreference("pref_key_my_class_threshold_sample");

            updatePrefSummary(sPreferenceCommon.getFloat("my_class_threshold", 0.8f));
        }

        private void updatePrefSummary(final float threshold) {
            final int summary = (int)(threshold*100.0f);

            mMyClassThreshold.setSummary(summary + "%%" + ((summary < 100) ? "以上" : ""));
            mMyClassSample.setThreshold(threshold);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
            if(key.equals("pref_key_my_class_threshold")){
                final SharedPreferences.Editor editor = sPreferenceCommon.edit();
                float threshold = Float.parseFloat(pref.getString(key, "80")) / 100.0f;

                PortalDataProvider.setMyClassThreshold(threshold);

                editor.putFloat("my_class_threshold", threshold);
                editor.apply();

                updatePrefSummary(threshold);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
