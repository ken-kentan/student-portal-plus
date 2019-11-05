package jp.kentan.studentportalplus.ui.noticedetail

import androidx.lifecycle.*
import jp.kentan.studentportalplus.data.NoticeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoticeDetailViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    private val noticeId = MutableLiveData<Long>()

    val notice = noticeId.switchMap {
        noticeRepository.getFlow(it).asLiveData()
    }

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
