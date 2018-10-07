package jp.kentan.studentportalplus.ui.setting

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import jp.kentan.studentportalplus.R

class NotificationTypePreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_notification_type)

        (findPreference("notification_type_lecture_info") as ListPreference).bindSummaryToValue()
        (findPreference("notification_type_lecture_cancel") as ListPreference).bindSummaryToValue()
        (findPreference("notification_type_notice") as ListPreference).bindSummaryToValue()
    }

    private fun ListPreference.bindSummaryToValue() {
        summary = entry

        setOnPreferenceChangeListener { preference, newValue ->
            val listPreference = preference as ListPreference
            val index = listPreference.findIndexOfValue(newValue.toString())

            listPreference.summary = listPreference.entries[index]

            return@setOnPreferenceChangeListener true
        }
    }
}