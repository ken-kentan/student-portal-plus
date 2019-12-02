package jp.kentan.studentportalplus.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.databinding.PreferenceSubjectSimilarSampleBinding
import jp.kentan.studentportalplus.ui.CommonBindingAdapter
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import jp.kentan.studentportalplus.util.executeAfter

class SimilarSubjectSamplePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle
) : Preference(context, attrs, defStyleAttr) {

    companion object {
        private val SUBJECTS = arrayOf("ABC実験ma", "ABC実験ma~mc", "ABC実験 ガイダンス", "XYZ実験ma")
    }

    private val attendImageViews = arrayOfNulls<ImageView>(4)

    var threshold: Float = 0.8f
        set(value) {
            field = value
            updateAttendImageViews()
        }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.isClickable = false

        val binding = PreferenceSubjectSimilarSampleBinding.bind(holder.itemView)
        binding.executePendingBindings()

        var indexChar = 'A'

        listOf(
            binding.itemLectureInformation1,
            binding.itemLectureInformation2,
            binding.itemLectureInformation3,
            binding.itemLectureInformation4
        ).forEachIndexed { index, item ->
            item.executeAfter {
                dateTextView.text = "2019/01/0${index + 1}"
                subjectTextView.text = SUBJECTS[index]
                detailTextView.text = "詳細テキスト${indexChar++}"
            }

            attendImageViews[index] = item.attendImageView
        }

        updateAttendImageViews()
    }

    private fun updateAttendImageViews() {
        attendImageViews.forEachIndexed { index, view ->
            if (view == null) {
                return@forEachIndexed
            }

            val attendType = when {
                index <= 0 -> AttendCourse.Type.PORTAL
                JaroWinklerDistance.getDistance(
                    SUBJECTS[0],
                    SUBJECTS[index]
                ) >= threshold -> AttendCourse.Type.SIMILAR
                else -> AttendCourse.Type.NOT
            }

            CommonBindingAdapter.setAttendType(view, attendType)
        }
    }
}
