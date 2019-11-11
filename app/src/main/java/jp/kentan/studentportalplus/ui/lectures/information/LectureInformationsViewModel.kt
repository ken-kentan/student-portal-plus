package jp.kentan.studentportalplus.ui.lectures.information

import androidx.lifecycle.*
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class LectureInformationsViewModel @Inject constructor(
    lectureInfoRepository: LectureInformationRepository
) : ViewModel() {

    private val _query = MutableLiveData(LectureQuery())
    val query: LectureQuery
        get() = requireNotNull(_query.value)

    val lectureInfoList: LiveData<List<LectureInformation>> =
        lectureInfoRepository.getListFlow(_query.asFlow()).asLiveData()

    val queryText: String?
        get() = query.text

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
