package jp.kentan.studentportalplus.ui.noticedetail

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.util.asLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoticeDetailViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    private val noticeId = MutableLiveData<Long>()

    val notice = noticeId.switchMap {
        noticeRepository.getAsFlow(it).asLiveData()
    }

    val finishWithNotFoundError = MediatorLiveData<Unit>().apply {
        addSource(notice) {
            if (it == null) value = Unit
        }
    }.asLiveData()

    fun onActivityCreate(id: Long) {
        viewModelScope.launch {
            noticeRepository.setRead(id)
        }

        noticeId.value = id
    }

    fun onFavoriteClick() {
        val notice = notice.value ?: return

        viewModelScope.launch {
            noticeRepository.update(notice.copy(isFavorite = !notice.isFavorite))
        }
    }
}
