package jp.kentan.studentportalplus.ui.lectureinformation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class LectureInformationViewModel @Inject constructor(
    private val lectureInfoRepository: LectureInformationRepository
) : ViewModel() {

    val lectureInfoList = lectureInfoRepository.getListFlow().asLiveData()

    private val _startDetailActivity = MutableLiveData<Event<Long>>()
    val startDetailActivity: LiveData<Event<Long>>
        get() = _startDetailActivity

    val onItemClick = { id: Long ->
        _startDetailActivity.value = Event(id)
    }
}
