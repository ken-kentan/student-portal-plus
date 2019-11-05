package jp.kentan.studentportalplus.ui.lecturecancellation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class LectureCancellationViewModel @Inject constructor(
    private val lectureCancelRepository: LectureCancellationRepository
) : ViewModel() {

    val lectureCancelList = lectureCancelRepository.getListFlow().asLiveData()

    private val _startDetailActivity = MutableLiveData<Event<Long>>()
    val startDetailActivity: LiveData<Event<Long>>
        get() = _startDetailActivity

    val onItemClick = { id: Long ->
        _startDetailActivity.value = Event(id)
    }
}
