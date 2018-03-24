package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.Notice
import jp.kentan.studentportalplus.util.toShortString
import org.jetbrains.anko.coroutines.experimental.bg

class NoticeViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    private var notice: Notice? = null

    fun getNotice(id: Long) = bg {
        notice = portalRepository.getNoticeById(id)
        return@bg notice
    }

    fun getNoticeShareText(context: Context): Pair<String, String> {
        val data = notice ?: return Pair("", "")

        val sb = StringBuilder()

        sb.append(context.getString(R.string.text_share_title, data.title))

        if (data.detail != null) {
            sb.append(context.getString(R.string.text_share_detail, data.detail))
        }

        if (data.link != null) {
            sb.append(context.getString(R.string.text_share_link, data.link))
        }

        sb.append(context.getString(R.string.text_share_created_date, data.createdDate.toShortString()))

        return Pair(data.title, sb.toString())
    }

    fun isFavorite() = notice?.isFavorite ?: false

    fun setFavorite(isFavorite: Boolean) {
        bg {
            val data = notice?.copy(isFavorite = isFavorite) ?: return@bg

            portalRepository.update(data)

            notice = data
        }
    }
}