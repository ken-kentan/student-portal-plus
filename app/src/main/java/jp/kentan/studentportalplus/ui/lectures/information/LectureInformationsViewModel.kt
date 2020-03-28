package jp.kentan.studentportalplus.ui.lectures.information

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.Preferences
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class LectureInformationsViewModel @Inject constructor(
    lectureInfoRepository: LectureInformationRepository,
    private val preferences: Preferences
) : ViewModel() {

    private val _query = MutableLiveData(
        LectureQuery(order = preferences.lectureInformationsOrder)
    )
    val query: LectureQuery
        get() = requireNotNull(_query.value)

    val queryText: String?
        get() = query.text

    val lectureInfoList: LiveData<List<LectureInformation>> =
        lectureInfoRepository.getAllAsFlow(_query.asFlow()).asLiveData()

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

        preferences.lectureInformationsOrder = query.order
    }
}
