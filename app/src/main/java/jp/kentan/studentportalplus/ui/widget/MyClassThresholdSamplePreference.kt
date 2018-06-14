package jp.kentan.studentportalplus.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import kotlinx.android.synthetic.main.sample_my_class_threshold.view.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource

class MyClassThresholdSamplePreference(context: Context, attributeSet: AttributeSet) : Preference(context, attributeSet) {

    private companion object {
        val SUBJECTS = arrayOf("ABC実験ma", "ABC実験ma~mc", "ABC実験 ガイダンス", "XYZ実験ma")
        val STRING_DISTANCE = JaroWinklerDistance()
    }

    private val iconViews = arrayOfNulls<ImageView>(4)
    private var threshold: Float = 0.8f

    init {
        widgetLayoutResource = R.layout.sample_my_class_threshold
    }

    @SuppressLint("SetTextI18n")
    override fun onBindView(view: View) {
        super.onBindView(view)

        val itemList = listOf<View>(view.lecture1, view.lecture2, view.lecture3, view.lecture4)

        var indexChar = 'A'
        itemList.forEachIndexed { index, item ->
            item.find<TextView>(R.id.date).text = "2018/01/0${index+1}"
            item.find<TextView>(R.id.subject).text = SUBJECTS[index]
            item.find<TextView>(R.id.detail).text = "詳細テキスト${indexChar++}"

            iconViews[index] = item.find(R.id.attend_icon)
        }

        updateIconView()
    }

    private fun updateIconView() {
        iconViews.forEachIndexed { index, icon ->
            if (index <= 0) {
                icon?.imageResource = R.drawable.ic_lecture_attend
                return@forEachIndexed
            }

            if (STRING_DISTANCE.getDistance(SUBJECTS[0], SUBJECTS[index]) >= threshold) {
                icon?.imageResource = R.drawable.ic_lecture_attend_similar
            } else {
                icon?.imageResource = R.drawable.ic_lecture_attend_not
            }
        }
    }

    fun updateThreshold(percent: Int) {
        threshold = percent / 100f

        updateIconView()
    }
}