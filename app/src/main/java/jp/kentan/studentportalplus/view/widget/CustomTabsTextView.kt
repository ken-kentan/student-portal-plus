package jp.kentan.studentportalplus.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.parseAsHtml
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.view.text.LinkTransformationMethod
import java.util.regex.Pattern


class CustomTabsTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        /**
         * Support custom SPAN class
         * https://portal.student.kit.ac.jp/css/common/wb_common.css
         */
        private val HTML_TAG_MAP by lazy(LazyThreadSafetyMode.NONE) {
            mapOf<Pattern, String>(
                Pattern.compile("<span class=\"col_red\">(.*?)</span>") to "<font color=\"#ff0000\">\$1</font>",
                Pattern.compile("<span class=\"col_green\">(.*?)</span>") to "<font color=\"#008000\">\$1</font>",
                Pattern.compile("<span class=\"col_blue\">(.*?)</span>") to "<font color=\"#0000ff\">\$1</font>",
                Pattern.compile("<span class=\"col_orange\">(.*?)</span>") to "<font color=\"#ffa500\">\$1</font>",
                Pattern.compile("<span class=\"col_white\">(.*?)</span>") to "<font color=\"#ffffff\">\$1</font>",
                Pattern.compile("<span class=\"col_black\">(.*?)</span>") to "<font color=\"#000000\">\$1</font>",
                Pattern.compile("<span class=\"col_gray\">(.*?)</span>") to "<font color=\"#999999\">\$1</font>",
                Pattern.compile("<a href=\"(.*?)\"(.*?)\">(.*?)</a>") to "\$3( \$1 )",
                Pattern.compile("<span class=\"u_line\">(.*?)</span>") to "<u>\$1</u>",
                Pattern.compile("([A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}?)") to " \$1 "
            )
        }
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CustomTabsTextView, defStyleAttr, 0)
            .apply {
                setHtml(getString(R.styleable.CustomTabsTextView_html))
                recycle()
            }

        transformationMethod = LinkTransformationMethod(context)
    }

    fun setHtml(html: String?) {
        var htmlText = html ?: run {
            text = null
            return
        }

        HTML_TAG_MAP.forEach { (pattern, span) ->
            htmlText = pattern.matcher(htmlText).replaceAll(span)
        }

        text = htmlText.parseAsHtml()
    }
}