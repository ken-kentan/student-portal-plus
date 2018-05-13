package jp.kentan.studentportalplus.ui

import android.app.FragmentTransaction
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebView
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.BuildConfig
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.NotifyType
import jp.kentan.studentportalplus.notification.SyncScheduler
import jp.kentan.studentportalplus.ui.span.CustomTitle
import jp.kentan.studentportalplus.ui.widget.MyClassThresholdSamplePreference
import jp.kentan.studentportalplus.util.getSyncIntervalMinutes
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast
import javax.inject.Inject


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = CustomTitle(this, getString(R.string.title_activity_settings))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, PreferencesFragment())
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        val count = fragmentManager.backStackEntryCount

        if (count > 0) {
            fragmentManager.popBackStackImmediate()
            return
        }

        super.onBackPressed()
    }

    class PreferencesFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        @Inject
        lateinit var portalRepository: PortalRepository

        private lateinit var shibbolethLastLoginDate: Preference
        private lateinit var syncInterval: ListPreference
        private lateinit var notifyContents: Preference
        private lateinit var notifyVibration: Preference
        private lateinit var notifyLed: Preference

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            AndroidInjection.inject(this)

            val screen = preferenceScreen

            shibbolethLastLoginDate = screen.findPreference("shibboleth_last_login_date")
            syncInterval = screen.findPreference("sync_interval") as ListPreference
            notifyContents = screen.findPreference("notify_contents")
            notifyVibration = screen.findPreference("enable_notify_vibration")
            notifyLed = screen.findPreference("enable_notify_led")

            notifyContents.setOnPreferenceClickListener {
                fragmentManager
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(android.R.id.content, NotifyContentsFragment())
                        .addToBackStack(null)
                        .commit()

                return@setOnPreferenceClickListener true
            }
            screen.findPreference("my_class_threshold").setOnPreferenceClickListener {
                fragmentManager
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(android.R.id.content, MyClassThresholdFragment())
                        .addToBackStack(null)
                        .commit()

                return@setOnPreferenceClickListener true
            }


            val enable = defaultSharedPreferences.getBoolean("enable_sync", true)

            syncInterval.isEnabled = enable
            notifyContents.isEnabled = enable
            notifyVibration.isEnabled = enable
            notifyLed.isEnabled = enable

            setupSummary(screen)
        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, pref: Preference): Boolean {
            when (pref.key) {
                "reset" -> {
                    AlertDialog.Builder(activity)
                            .setIcon(R.drawable.ic_warning)
                            .setTitle("ポータルデータ消去")
                            .setMessage(R.string.msg_warn_reset)
                            .setPositiveButton(R.string.action_yes) { _, _ ->
                                bg {
                                    portalRepository.deleteAll { success ->
                                        async(UI) {
                                            if (success) {
                                                longToast("すべてのポータルデータを消去しました")
                                            } else {
                                                longToast("消去に失敗しました")
                                            }
                                        }
                                    }
                                }
                            }
                            .setNegativeButton(R.string.action_no, null)
                            .show()
                }
                "share" -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text_share_app))
                    startActivity(intent)
                }
                "terms" -> {
                    showWebViewDialog("利用規約・プライバシーポリシー", getString(R.string.url_terms))
                }
                "oss_license" -> {
                    showWebViewDialog("オープンソースライセンス", "file:///android_asset/licenses.html")
                }
            }
            return super.onPreferenceTreeClick(preferenceScreen, pref)
        }

        override fun onSharedPreferenceChanged(pref: SharedPreferences, key: String) {
            when (key) {
                "shibboleth_last_login_date" -> {
                    shibbolethLastLoginDate.summary = pref.getString("shibboleth_last_login_date", "なし")
                }
                "enable_sync" -> {
                    val enable = pref.getBoolean("enable_sync", true)

                    syncInterval.isEnabled = enable
                    notifyContents.isEnabled = enable
                    notifyVibration.isEnabled = enable
                    notifyLed.isEnabled = enable

                    if (enable) {
                        SyncScheduler.schedule(activity)
                    } else {
                        SyncScheduler.cancel()
                    }
                }
                "sync_interval" -> {
                    val interval = pref.getSyncIntervalMinutes()

                    syncInterval.summary = if (interval >= 60) {
                        "${interval / 60}時間毎に更新する"
                    } else {
                        "${interval}分毎に更新する"
                    }

                    SyncScheduler.schedule(activity)
                }
            }
        }

        override fun onResume() {
            super.onResume()
            defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        private fun setupSummary(screen: PreferenceScreen) {
            val pref = defaultSharedPreferences

            screen.findPreference("shibboleth_last_login_date").summary = pref.getString("shibboleth_last_login_date", "なし")
            screen.findPreference("version").summary = BuildConfig.VERSION_NAME

            val interval = pref.getSyncIntervalMinutes()
            syncInterval.summary = if (interval >= 60) {
                "${interval / 60}時間毎に更新する"
            } else {
                "${interval}分毎に更新する"
            }
        }

        private fun showWebViewDialog(title: String, url: String) {
            val webView = WebView(activity)
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webView.settings.setAppCacheEnabled(false)
            webView.loadUrl(url)

            AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setView(webView)
                    .setPositiveButton("OK", null)
                    .setOnDismissListener { webView.destroy() }
                    .show()
        }
    }

    class NotifyContentsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        private lateinit var lectureInfo: Preference
        private lateinit var lectureCancel: Preference
        private lateinit var notice: Preference

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_notify_contents)

            val screen = preferenceScreen
            val pref = defaultSharedPreferences

            lectureInfo = screen.findPreference("notify_type_lecture_info")
            lectureCancel = screen.findPreference("notify_type_lecture_cancel")
            notice = screen.findPreference("notify_type_notice")

            lectureInfo.summary = NotifyType.valueOf(pref.getString("notify_type_lecture_info", NotifyType.ALL.name)).displayName
            lectureCancel.summary = NotifyType.valueOf(pref.getString("notify_type_lecture_cancel", NotifyType.ALL.name)).displayName
            notice.summary = NotifyType.valueOf(pref.getString("notify_type_notice", NotifyType.ALL.name)).displayName

        }

        override fun onSharedPreferenceChanged(pref: SharedPreferences, key: String) {

            val preference = when (key) {
                "notify_type_lecture_info" -> lectureInfo
                "notify_type_lecture_cancel" -> lectureCancel
                "notify_type_notice" -> notice
                else -> return
            }

            preference.summary = NotifyType.valueOf(pref.getString(key, NotifyType.ALL.name)).displayName
        }

        override fun onResume() {
            super.onResume()
            defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }
    }


    class MyClassThresholdFragment : PreferenceFragment() {

        private lateinit var threshold: Preference
        private lateinit var sample: MyClassThresholdSamplePreference

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_my_class_threshold)

            val screen = preferenceScreen

            threshold = screen.findPreference("my_class_threshold")
            sample    = screen.findPreference("my_class_threshold_sample") as MyClassThresholdSamplePreference

            val currentPercent = defaultSharedPreferences.getString("my_class_threshold", "80").toIntOrNull() ?: 80

            threshold.summary = if (currentPercent < 100) "$currentPercent%%以上" else "100%%"
            sample.updateThreshold(currentPercent)

            threshold.setOnPreferenceChangeListener { _, newValue ->
                val percent = newValue.toString().toIntOrNull() ?: 80

                threshold.summary = if (percent < 100) "$newValue%%以上" else "100%%"
                sample.updateThreshold(percent)

                return@setOnPreferenceChangeListener true
            }
        }
    }
}
