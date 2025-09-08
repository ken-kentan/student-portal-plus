package jp.kentan.studentportalplus.ui.notice.detail

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.switchMap
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.ui.SingleLiveData
import jp.kentan.studentportalplus.util.formatYearMonthDay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NoticeDetailViewModel(
        private val context: Application,
        private val portalRepository: PortalRepository
) : AndroidViewModel(context) {

    private val idLiveData = MutableLiveData<Long>()

    val notice: LiveData<Notice> = idLiveData.switchMap { id ->
        portalRepository.getNotice(id)
    }
    val snackbar = SingleLiveData<Int>()
    val indefiniteSnackbar = SingleLiveData<Int>()
    val share = SingleLiveData<Intent>()
    val errorNotFound = SingleLiveData<Unit>()

    private val noticeObserver = Observer<Notice> { data ->
        if (data == null) {
            errorNotFound.value = Unit
        } else if (!data.isRead) {
            portalRepository.updateNotice(data.copy(isRead = true))
        }
    }

    init {
        notice.observeForever(noticeObserver)
    }

    fun onActivityCreated(id: Long) {
        idLiveData.value = id
    }

    fun onFavoriteClick(data: Notice?) {
        data ?: return

        GlobalScope.launch {
            val isFavorite = !data.isFavorite
            val isSuccess = portalRepository.updateNotice(data.copy(isFavorite = isFavorite)).await()

            if (isSuccess) {
                snackbar.postValue((if (isFavorite) R.string.msg_set_favorite else R.string.msg_reset_favorite))
            } else {
                indefiniteSnackbar.postValue(R.string.error_update)
            }
        }
    }

    fun onShareClick() {
        val data = notice.value ?: return

        val text = StringBuilder(context.getString(R.string.share_title, data.title))

        data.detailText?.let {
            text.append(context.getString(R.string.share_detail, it))
        }
        data.link?.let {
            text.append(context.getString(R.string.share_link, it))
        }
        text.append(context.getString(R.string.share_created_date, data.createdDate.formatYearMonthDay()))

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, data.title)
            putExtra(Intent.EXTRA_TEXT, text.toString())
        }

        share.value = Intent.createChooser(intent, null)
    }

    override fun onCleared() {
        notice.removeObserver(noticeObserver)
        super.onCleared()
    }
}