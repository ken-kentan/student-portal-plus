package jp.kentan.studentportalplus.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.BuildConfig
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.util.requirePreference
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class GeneralPreferenceFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var localPreference: LocalPreferences

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        AndroidSupportInjection.inject(this)

        setPreferencesFromResource(R.xml.pref_general, rootKey)

        requirePreference<Preference>("login").setOnPreferenceClickListener {
            onNavDestinationClicked(R.id.login_activity, isFragment = false)
            return@setOnPreferenceClickListener true
        }

        requirePreference<Preference>("notification_type").setOnPreferenceClickListener {
            onNavDestinationClicked(R.id.notification_type_preference_fragment)
            return@setOnPreferenceClickListener true
        }

        requirePreference<Preference>("similar_subject_threshold").setOnPreferenceClickListener {
            onNavDestinationClicked(R.id.similar_subject_preference_fragment)
            return@setOnPreferenceClickListener true
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
            onNavDestinationClicked(R.id.oss_licenses_menu_activity, isFragment = false)
            return@setOnPreferenceClickListener true
        }
    }

    override fun onResume() {
        super.onResume()

        requirePreference<Preference>("shibboleth_last_login_date").summary =
            dateFormat.format(localPreference.shibbolethLastLoginDate)
    }

    private fun onNavDestinationClicked(@IdRes resId: Int, isFragment: Boolean = true) {
        val builder = NavOptions.Builder()
            .setLaunchSingleTop(true)

        if (isFragment) {
            builder.setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                .setPopEnterAnim(androidx.navigation.ui.R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(androidx.navigation.ui.R.anim.nav_default_pop_exit_anim)
        }

        findNavController().navigate(resId, null, builder.build())
    }
}
