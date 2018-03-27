package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.util.toShortString
import org.jetbrains.anko.coroutines.experimental.bg

class NoticeViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    private var data: Notice? = null

    fun get(id: Long) = bg {
        data = portalRepository.getNoticeById(id)
        return@bg data
    }

    fun getShareText(context: Context): Pair<String, String> {
        val data = data ?: return Pair("", "")

        val sb = StringBuilder()

        sb.append(context.getString(R.string.text_share_title, data.title))

        if (data.detailText != null) {
            sb.append(context.getString(R.string.text_share_detail, data.detailText))
        }

        if (data.link != null) {
            sb.append(context.getString(R.string.text_share_link, data.link))
        }

        sb.append(context.getString(R.string.text_share_created_date, data.createdDate.toShortString()))

        return Pair(data.title, sb.toString())
    }

    fun isFavorite() = data?.isFavorite ?: throw NullPointerException("Not found a target data")

    fun setFavorite(isFavorite: Boolean) {
        bg {
            val data = data?.copy(isFavorite = isFavorite) ?: return@bg

            portalRepository.update(data)

            this.data = data
        }
    }
}