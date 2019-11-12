package jp.kentan.studentportalplus.ui.notices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class NoticesViewModel @Inject constructor(
    noticeRepository: NoticeRepository
) : ViewModel() {

    val lectureInfoList = noticeRepository.getListFlow().asLiveData()

    private val _startDetailActivity = MutableLiveData<Event<Long>>()
    val startDetailActivity: LiveData<Event<Long>>
        get() = _startDetailActivity

    val onItemClick = { id: Long ->
        _startDetailActivity.value = Event(id)
    }
}
