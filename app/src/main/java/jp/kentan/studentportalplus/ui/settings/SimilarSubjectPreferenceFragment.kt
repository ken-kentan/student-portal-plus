package jp.kentan.studentportalplus.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import jp.kentan.studentportalplus.R

class SimilarSubjectPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_similar_subject, rootKey)
    }
}
