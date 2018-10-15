package jp.kentan.studentportalplus.view.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import jp.kentan.studentportalplus.util.getSimilarSubjectThresholdFloat
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.find

class SimilarSubjectSamplePreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle
) : Preference(context, attrs, defStyleAttr) {

    private companion object {
        val SUBJECTS = arrayOf("ABC実験ma", "ABC実験ma~mc", "ABC実験 ガイダンス", "XYZ実験ma")
    }

    private val stringDistance = JaroWinklerDistance()
    private val iconViews = arrayOfNulls<ImageView>(4)
    private var threshold = context.defaultSharedPreferences.getSimilarSubjectThresholdFloat()

    init {
        widgetLayoutResource = R.layout.preference_subject_similar_sample
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.apply {
            isClickable = false

            val itemList = listOf<View>(find(R.id.lecture1), find(R.id.lecture2), find(R.id.lecture3), find(R.id.lecture4))

            var indexChar = 'A'
            itemList.forEachIndexed { index, item ->
                item.find<TextView>(R.id.date).text = "2018/01/0${index + 1}"
                item.find<TextView>(R.id.subject).text = SUBJECTS[index]
                item.find<TextView>(R.id.detail).text = "詳細テキスト${indexChar++}"

                iconViews[index] = item.find(R.id.attend_image)
            }
        }

        updateIconView()
    }

    private fun updateIconView() {
        iconViews.forEachIndexed { index, icon ->
            if (index <= 0) {
                icon?.setImageResource(R.drawable.ic_lecture_attend)
                return@forEachIndexed
            }

            if (stringDistance.getDistance(SUBJECTS[0], SUBJECTS[index]) >= threshold) {
                icon?.setImageResource(R.drawable.ic_lecture_attend_similar)
            } else {
                icon?.setImageResource(R.drawable.ic_lecture_attend_not)
            }
        }
    }

    fun updateThreshold(percent: Int) {
        threshold = percent / 100f

        updateIconView()
    }
}