package jp.kentan.studentportalplus.ui.lectures.cancellation

import androidx.lifecycle.*
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class LectureCancellationsViewModel @Inject constructor(
    lectureCancelRepository: LectureCancellationRepository
) : ViewModel() {

    private val _query = MutableLiveData(LectureQuery())
    val query: LectureQuery
        get() = requireNotNull(_query.value)

    val queryText: String?
        get() = query.text

    val lectureCancelList = lectureCancelRepository.getListFlow(_query.asFlow()).asLiveData()

    private val _startDetailActivity = MutableLiveData<Event<Long>>()
    val startDetailActivity: LiveData<Event<Long>>
        get() = _startDetailActivity

    val onItemClick = { id: Long ->
        _startDetailActivity.value = Event(id)
    }

    fun onQueryTextChange(newText: String?) {
        _query.value = query.copy(text = newText)
    }

    fun onFilterApplyClick(query: LectureQuery) {
        _query.value = query
    }
}
