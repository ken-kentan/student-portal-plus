package jp.kentan.studentportalplus.view.text

import android.content.Context
import android.graphics.Rect
import android.text.Spannable
import android.text.Spanned
import android.text.method.TransformationMethod
import android.text.style.URLSpan
import android.util.Patterns
import android.view.View
import android.widget.TextView

class LinkTransformationMethod(
        private val context: Context
) : TransformationMethod {

    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        if (view is TextView) {
            if (view.text.isNullOrBlank() || view.text !is Spannable) {
                return source
            }

            val text = view.text as Spannable
            val spans = text.getSpans(0, view.length(), URLSpan::class.java)
            spans.forEach { span ->
                val url = span.url

                if (Patterns.WEB_URL.matcher(url).matches()) {
                    val start = text.getSpanStart(span)
                    val end = text.getSpanEnd(span)

                    text.removeSpan(span)
                    text.setSpan(CustomTabsUrlSpan(context, url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            return text
        }

        return source
    }

    override fun onFocusChanged(view: View?, sourceText: CharSequence?, focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {}
}