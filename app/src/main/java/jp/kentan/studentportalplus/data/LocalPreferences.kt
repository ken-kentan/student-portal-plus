package jp.kentan.studentportalplus.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import jp.kentan.studentportalplus.data.vo.LectureQuery
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow

class LocalPreferences(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val IS_GRID_TIMETABLE_LAYOUT = "is_grid_timetable_layout"
        private const val IS_ENABLED_PDF_OPEN_WITH_GDOCS = "is_enabled_pdf_open_with_gdocs"
        private const val LECTURE_INFORMATIONS_ORDER = "lecture_informations_order"
        private const val LECTURE_CANCELLATIONS_ORDER = "lecture_cancellations_order"
        private const val SIMILAR_SUBJECT_THRESHOLD = "similar_subject_threshold"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).apply {
        registerOnSharedPreferenceChangeListener(this@LocalPreferences)
    }

    var isGridTimetableLayout: Boolean
        get() = sharedPreferences.getBoolean(IS_GRID_TIMETABLE_LAYOUT, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_GRID_TIMETABLE_LAYOUT, value)
        }

    var isEnabledPdfOpenWithGdocs: Boolean
        get() = sharedPreferences.getBoolean(IS_ENABLED_PDF_OPEN_WITH_GDOCS, true)
        set(value) = sharedPreferences.edit {
            putBoolean(IS_ENABLED_PDF_OPEN_WITH_GDOCS, value)
        }

    var lectureInformationsOrder: LectureQuery.Order
        get() = LectureQuery.Order.valueOf(
            sharedPreferences.getString(
                LECTURE_INFORMATIONS_ORDER, null
            ) ?: LectureQuery.Order.UPDATED_DATE.name
        )
        set(value) = sharedPreferences.edit {
            putString(LECTURE_INFORMATIONS_ORDER, value.name)
        }

    var lectureCancellationsOrder: LectureQuery.Order
        get() = LectureQuery.Order.valueOf(
            sharedPreferences.getString(
                LECTURE_CANCELLATIONS_ORDER, null
            ) ?: LectureQuery.Order.UPDATED_DATE.name
        )
        set(value) = sharedPreferences.edit {
            putString(LECTURE_CANCELLATIONS_ORDER, value.name)
        }

    private val similarSubjectThreshold: Int
        get() = sharedPreferences.getInt(SIMILAR_SUBJECT_THRESHOLD, 80)

    private val similarSubjectThresholdChannel =
        ConflatedBroadcastChannel(similarSubjectThreshold / 100F)
    val similarSubjectThresholdFlow = similarSubjectThresholdChannel.asFlow()

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == SIMILAR_SUBJECT_THRESHOLD) {
            similarSubjectThresholdChannel.offer(similarSubjectThreshold / 100F)
        }
    }
}
