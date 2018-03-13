package jp.kentan.studentportalplus.ui.span

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import jp.kentan.studentportalplus.R


/**
 * Custom font(Orkney) spannable string
 * @note https://www.fontsquirrel.com/fonts/orkney
 */
class CustomTitle(val context: Context, val title: String) : SpannableString(title) {

    private companion object {
        var typefaceSpan: MetricAffectingSpan? = null
    }

    init {
        typefaceSpan = typefaceSpan ?: ResourcesCompat.getFont(context, R.font.orkney)?.toSpan()

        if (typefaceSpan != null) {
            setSpan(typefaceSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * Create MetricAffectingSpan from Typeface
     */
    private fun Typeface.toSpan(): MetricAffectingSpan {
        return object : MetricAffectingSpan() {
            override fun updateMeasureState(p: TextPaint) {
                p.typeface = this@toSpan
            }
            override fun updateDrawState(tp: TextPaint) {
                tp.typeface = this@toSpan
            }
        }
    }
}