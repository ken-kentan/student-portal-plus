package jp.kentan.studentportalplus.ui.settings

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.requirePreference

class NotificationTypePreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_notification_type, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()

        requirePreference<ListPreference>("notification_type_lecture_info")
            .summaryProvider = summaryProvider

        requirePreference<ListPreference>("notification_type_lecture_cancel")
            .summaryProvider = summaryProvider

        requirePreference<ListPreference>("notification_type_notice")
            .summaryProvider = summaryProvider
    }
}
