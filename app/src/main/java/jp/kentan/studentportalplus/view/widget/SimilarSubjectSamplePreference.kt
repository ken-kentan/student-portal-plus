package jp.kentan.studentportalplus.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import jp.kentan.studentportalplus.data.vo.MyCourseType
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

    private val myCourseImageViews = arrayOfNulls<ImageView>(4)

    var threshold: Float = 0.8f
        set(value) {
            field = value
            updateMyCourseImageViews()
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
                dateTextView.text = "2020/01/0${index + 1}"
                subjectTextView.text = SUBJECTS[index]
                detailTextView.text = "詳細テキスト${indexChar++}"
            }

            myCourseImageViews[index] = item.myCourseImageView
        }

        updateMyCourseImageViews()
    }

    private fun updateMyCourseImageViews() {
        myCourseImageViews.forEachIndexed { index, view ->
            if (view == null) {
                return@forEachIndexed
            }

            val myCourseType = when {
                index <= 0 -> MyCourseType.NOT_EDITABLE
                JaroWinklerDistance.getDistance(
                    SUBJECTS[0],
                    SUBJECTS[index]
                ) >= threshold -> MyCourseType.SIMILAR
                else -> MyCourseType.NOT_FOUND
            }

            CommonBindingAdapter.setMyCourseType(view, myCourseType)
        }
    }
}
