package jp.kentan.studentportalplus.ui.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.getSimilarSubjectThreshold
import jp.kentan.studentportalplus.view.widget.SimilarSubjectSamplePreference
import org.jetbrains.anko.defaultSharedPreferences

class SimilarSubjectPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_similar_subject)

        val samplePreference = findPreference("similar_threshold_sample") as SimilarSubjectSamplePreference

        findPreference("similar_subject_threshold").apply {
            requireContext().defaultSharedPreferences.getSimilarSubjectThreshold().let { percent ->
                summary = if (percent < 100) "$percent%%以上" else "$percent%%"
            }

            setOnPreferenceChangeListener { _, newValue ->
                val percent = newValue.toString().toIntOrNull() ?: 80
                summary = if (percent < 100) "$percent%%以上" else "$percent%%"

                samplePreference.updateThreshold(percent)

                return@setOnPreferenceChangeListener true
            }
        }
    }

}