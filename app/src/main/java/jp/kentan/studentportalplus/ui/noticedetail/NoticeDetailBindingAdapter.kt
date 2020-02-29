package jp.kentan.studentportalplus.ui.noticedetail

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.util.formatYearMonthDay
import jp.kentan.studentportalplus.view.widget.NoticeFloatingActionButton

object NoticeDetailBindingAdapter {
    @JvmStatic
    @BindingAdapter("notice")
    fun setNotice(view: NoticeFloatingActionButton, notice: Notice?) {
        view.setNotice(notice)
    }

    @JvmStatic
    @BindingAdapter("noticeDate")
    fun setNoticeDate(view: TextView, notice: Notice?) {
        if (notice == null) {
            view.text = null
            return
        }

        view.text = view.context.getString(
            R.string.notice_detail_created_date,
            notice.createdDate.formatYearMonthDay()
        )
    }
}
