package jp.kentan.studentportalplus.ui.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.util.requirePreference
import jp.kentan.studentportalplus.view.widget.SimilarSubjectSamplePreference
import javax.inject.Inject

class SimilarSubjectPreferenceFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var localPreferences: LocalPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        AndroidSupportInjection.inject(this)
        setPreferencesFromResource(R.xml.pref_similar_subject, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requirePreference<Preference>("similar_subject_threshold")
            .summaryProvider = Preference.SummaryProvider<ListPreference> { it.entry }

        val similarSubjectSamplePreference =
            requirePreference<SimilarSubjectSamplePreference>("similar_subject_sample")

        localPreferences.similarSubjectThresholdFlow.asLiveData().observe(viewLifecycleOwner) {
            similarSubjectSamplePreference.threshold = it
        }
    }
}
