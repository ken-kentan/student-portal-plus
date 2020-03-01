package jp.kentan.studentportalplus.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.BuildConfig
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.notification.SummaryNotificationHelper
import jp.kentan.studentportalplus.util.requirePreference
import jp.kentan.studentportalplus.work.sync.SyncWorker
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class GeneralPreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    companion object {
        private const val TAG = "GeneralPrefFragment"
    }

    @Inject
    lateinit var localPreferences: LocalPreferences

    private val onIsEnabledSyncPreferenceChangeListener =
        Preference.OnPreferenceChangeListener { _, newValue ->
            val isEnabled = newValue as Boolean

            try {
                val workManager = WorkManager.getInstance(requireContext())

                if (isEnabled) {
                    val syncWorkRequest =
                        SyncWorker.buildPeriodicWorkRequest(localPreferences.syncIntervalMinutes)

                    workManager.enqueueUniquePeriodicWork(
                        SyncWorker.NAME,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        syncWorkRequest
                    )

                    Log.d(TAG, "Enqueued a unique SyncWorker")
                } else {
                    workManager.cancelUniqueWork(SyncWorker.NAME)
                    Log.d(TAG, "Cancelled a unique SyncWorker")
                }

            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to update SyncWorker", e)
                return@OnPreferenceChangeListener false
            }

            requirePreference<Preference>("sync_interval_minutes").isEnabled = isEnabled
            requirePreference<Preference>("notification_type").isEnabled = isEnabled

            findPreference<Preference>("is_enabled_notification_vibration")?.isEnabled = isEnabled
            findPreference<Preference>("is_enabled_notification_led")?.isEnabled = isEnabled

            return@OnPreferenceChangeListener true
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        AndroidSupportInjection.inject(this)
        setPreferencesFromResource(R.xml.pref_general, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requirePreference<Preference>("version").setSummaryProvider { BuildConfig.VERSION_NAME }

        registerOnPreferenceClickListener("login")
        registerOnPreferenceClickListener("notification_type")
        registerOnPreferenceClickListener("similar_subject_threshold")
        registerOnPreferenceClickListener("terms")
        registerOnPreferenceClickListener("oss_licenses")
        registerOnPreferenceClickListener("share")

        requirePreference<Preference>("is_enabled_sync").onPreferenceChangeListener =
            onIsEnabledSyncPreferenceChangeListener

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requirePreference<Preference>("notification_settings").setOnPreferenceClickListener {
                val settingsIntent =
                    SummaryNotificationHelper.createNewlyChannelSettingsIntent(requireContext())
                startActivity(settingsIntent)

                return@setOnPreferenceClickListener true
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN)
        requirePreference<Preference>("shibboleth_last_login_date").summary =
            dateFormat.format(localPreferences.shibbolethLastLoginDate)
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "login" -> findNavController().navigate(R.id.action_general_preference_fragment_to_login_fragment)
            "notification_type" -> findNavController().navigate(R.id.action_general_preference_fragment_to_notification_type_preference_fragment)
            "similar_subject_threshold" -> findNavController().navigate(R.id.action_general_preference_fragment_to_similar_subject_preference_fragment)
            "terms" -> findNavController().navigate(R.id.action_general_preference_fragment_to_terms_custom_tabs)
            "oss_licenses" -> findNavController().navigate(R.id.action_general_preference_fragment_to_oss_licenses_menu_activity)
            "share" -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_share_app))
                }
                startActivity(intent)
            }
            else -> return false
        }

        return true
    }

    private fun registerOnPreferenceClickListener(key: String) {
        requirePreference<Preference>(key).onPreferenceClickListener = this
    }
}
