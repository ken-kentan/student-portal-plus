package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.util.toShortString
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.share

class NoticeViewModel(private val repository: PortalRepository) : ViewModel() {

    val noticeId = MutableLiveData<Long>()
    val notice: LiveData<Notice> = Transformations.map(noticeId) { id -> findNoticeById(id)}


    fun onClickShare(context: Context) {
        val data = notice.value ?: return

        val text = StringBuilder(context.getString(R.string.text_share_title, data.title))

        if (data.detailText.isNullOrEmpty()) {
            text.append(context.getString(R.string.text_share_detail, data.detailText))
        }
        if (data.link.isNullOrEmpty()) {
            text.append(context.getString(R.string.text_share_link, data.link))
        }
        text.append(context.getString(R.string.text_share_created_date, data.createdDate.toShortString()))

        context.share(text.toString(), data.title)
    }

    fun onClickFavorite(onUpdated: (isSuccess: Boolean, isFavorite: Boolean) -> Unit) {
        val data = findNoticeById(noticeId.value) ?: return
        val favorite = !data.isFavorite

        async(UI) {
            val success = bg { repository.update(data.copy(isFavorite = favorite)) }.await()
            onUpdated(success, favorite)
        }
    }

    private fun findNoticeById(id: Long?): Notice? {
        return if (id == null || id < 1) {
            null
        } else {
            repository.getNoticeById(id)
        }
    }
}