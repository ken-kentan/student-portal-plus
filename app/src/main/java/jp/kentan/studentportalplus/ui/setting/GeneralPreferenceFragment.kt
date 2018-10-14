package jp.kentan.studentportalplus.ui.setting

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentTransaction
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.BuildConfig
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.notification.NotificationController
import jp.kentan.studentportalplus.notification.SyncScheduler
import jp.kentan.studentportalplus.ui.web.WebActivity
import jp.kentan.studentportalplus.util.formatYearMonthDayHms
import jp.kentan.studentportalplus.util.getShibbolethLastLoginDate
import jp.kentan.studentportalplus.util.isEnabledSync
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.util.*
import javax.inject.Inject

class GeneralPreferenceFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var portalRepository: PortalRepository

    private val syncScheduler by lazy(LazyThreadSafetyMode.NONE) { SyncScheduler(requireContext()) }

    private lateinit var shibbolethLastLoginDate: Preference
    private lateinit var syncInterval: ListPreference
    private lateinit var notificationType: Preference
    private var isEnabledNotificationVibration: Preference? = null
    private var isEnabledNotificationLed: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)

        AndroidSupportInjection.inject(this)

        shibbolethLastLoginDate = findPreference("shibboleth_last_login_date")
        syncInterval = findPreference("sync_interval_minutes") as ListPreference
        notificationType = findPreference("notification_type")
        isEnabledNotificationVibration = findPreference("is_enabled_notification_vibration")
        isEnabledNotificationLed = findPreference("is_enabled_notification_led")

        val isEnabledSync = requireContext().defaultSharedPreferences.isEnabledSync()
        setEnabledSync(isEnabledSync)

        syncInterval.setOnPreferenceChangeListener { preference, newValue ->
            val listPreference = preference as ListPreference
            val index = listPreference.findIndexOfValue(newValue.toString())

            listPreference.summary = getString(R.string.pref_summary_sync_interval,
                    listPreference.entries[index])

            return@setOnPreferenceChangeListener true
        }

        notificationType.setOnPreferenceClickListener {
            commitFragment(NotificationTypePreferenceFragment())
            return@setOnPreferenceClickListener true
        }

        findPreference("similar_subject_threshold").setOnPreferenceClickListener {
            commitFragment(SimilarSubjectPreferenceFragment())
            return@setOnPreferenceClickListener true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            findPreference("notification_settings")?.setOnPreferenceClickListener {
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, NotificationController.NEWLY_CHANNEL_ID)
                }
                startActivity(intent)

                return@setOnPreferenceClickListener true
            }
        }

        setupSummary()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        when (key) {
            "shibboleth_last_login_date" -> {
                shibbolethLastLoginDate.summary = preferences.getFormatShibbolethLastLoginDate()
            }
            "is_enabled_sync" -> {
                val isEnabled = preferences.isEnabledSync()
                setEnabledSync(isEnabled)

                if (isEnabled) {
                    syncScheduler.schedule()
                } else {
                    syncScheduler.cancel()
                }
            }
            "sync_interval_minutes" -> syncScheduler.schedule()
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "reset" -> view?.run {
                showDeleteConfirmDialog(this)
            }
            "share" -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app))
                }
                startActivity(intent)
            }
            "terms" -> startActivity(WebActivity.createIntent(requireContext(), "Terms", getString(R.string.url_terms)))
            "license" -> startActivity(WebActivity.createIntent(requireContext(), "Licenses", getString(R.string.url_licenses)))
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onResume() {
        super.onResume()
        requireContext().defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        requireContext().defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    private fun commitFragment(fragment: PreferenceFragmentCompat) {
        requireFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit()
    }

    private fun setupSummary() {
        val pref = requireContext().defaultSharedPreferences

        shibbolethLastLoginDate.summary = pref.getFormatShibbolethLastLoginDate()
        syncInterval.summary = getString(R.string.pref_summary_sync_interval, syncInterval.entry)
        findPreference("version").summary = BuildConfig.VERSION_NAME
    }

    private fun setEnabledSync(isEnabled: Boolean) {
        syncInterval.isEnabled = isEnabled
        notificationType.isEnabled = isEnabled
        isEnabledNotificationVibration?.isEnabled = isEnabled
        isEnabledNotificationLed?.isEnabled = isEnabled
    }

    private fun showDeleteConfirmDialog(view: View) {
        AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_warning)
                .setTitle("ポータルデータ消去")
                .setMessage(R.string.msg_warn_reset)
                .setPositiveButton(R.string.action_yes) { _, _ ->
                    GlobalScope.launch(Dispatchers.Main) {
                        val isSuccess = portalRepository.deleteAll().await()

                        if (isSuccess) {
                            Snackbar.make(view, R.string.msg_delete_all, Snackbar.LENGTH_LONG).show()
                        } else {
                            val snackbar = Snackbar.make(view, R.string.error_delete, Snackbar.LENGTH_INDEFINITE)

                            snackbar.setAction(R.string.action_close) { snackbar.dismiss() }
                                    .show()
                        }
                    }
                }
                .setNegativeButton(R.string.action_no, null)
                .show()
    }

    private fun SharedPreferences.getFormatShibbolethLastLoginDate(): String {
        val time = getShibbolethLastLoginDate()
        return if (time <= 0) "unknown" else Date(time).formatYearMonthDayHms()
    }
}