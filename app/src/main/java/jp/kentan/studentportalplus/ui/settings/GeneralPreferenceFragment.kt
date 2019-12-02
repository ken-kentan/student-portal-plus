package jp.kentan.studentportalplus.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
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


class GeneralPreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    @Inject
    lateinit var localPreference: LocalPreferences

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
    }

    override fun onResume() {
        super.onResume()

        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN)
        requirePreference<Preference>("shibboleth_last_login_date").summary =
            dateFormat.format(localPreference.shibbolethLastLoginDate)
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "login" -> onNavDestinationClicked(R.id.login_activity, isFragment = false)
            "notification_type" -> onNavDestinationClicked(R.id.notification_type_preference_fragment)
            "similar_subject_threshold" -> onNavDestinationClicked(R.id.similar_subject_preference_fragment)
            "terms" -> onNavDestinationClicked(R.id.terms_custom_tabs, isFragment = false)
            "oss_licenses" -> onNavDestinationClicked(
                R.id.oss_licenses_menu_activity,
                isFragment = false
            )
            "share" -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.text_share_app))
                }
                startActivity(intent)
            }
            else -> return false
        }

        return true
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

    private fun registerOnPreferenceClickListener(key: String) {
        requirePreference<Preference>(key).onPreferenceClickListener = this
    }
}
