package jp.kentan.studentportalplus.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.BuildConfig
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class GeneralPreferenceFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var localPreference: LocalPreferences

    private lateinit var shibbolethLastLoginDatePreference: Preference

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        AndroidSupportInjection.inject(this)

        setPreferencesFromResource(R.xml.pref_general, rootKey)

        shibbolethLastLoginDatePreference = requirePreference("shibboleth_last_login_date")

        requirePreference<Preference>("login").setOnPreferenceClickListener {
            findNavController().navigate(R.id.login_activity)
            return@setOnPreferenceClickListener true
        }

        shibbolethLastLoginDatePreference.setSummaryProvider {
            dateFormat.format(localPreference.shibbolethLastLoginDate)
        }

        requirePreference<Preference>("version").setSummaryProvider { BuildConfig.VERSION_NAME }

        requirePreference<Preference>("share").setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.text_share_app))
            }
            startActivity(intent)

            return@setOnPreferenceClickListener true
        }

        requirePreference<Preference>("oss_licenses").setOnPreferenceClickListener {
            findNavController().navigate(R.id.oss_licenses_menu_activity)
            return@setOnPreferenceClickListener true
        }
    }

    private fun <T : Preference> requirePreference(key: String) =
        findPreference<T>(key) ?: throw NullPointerException("$key is not found.")
}
