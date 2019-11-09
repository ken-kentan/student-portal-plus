package jp.kentan.studentportalplus.ui.lectureinformation

import androidx.lifecycle.*
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class LectureInformationViewModel @Inject constructor(
    lectureInfoRepository: LectureInformationRepository
) : ViewModel() {

    private val query = MutableLiveData(LectureQuery())
    val queryText: String?
        get() = query.value?.text

    val lectureInfoList: LiveData<List<LectureInformation>> =
        lectureInfoRepository.getListFlow(query.asFlow()).asLiveData()

    private val _startDetailActivity = MutableLiveData<Event<Long>>()
    val startDetailActivity: LiveData<Event<Long>>
        get() = _startDetailActivity

    val onItemClick = { id: Long ->
        _startDetailActivity.value = Event(id)
    }

    fun onQueryTextChange(newText: String?) {
        val query = requireNotNull(query.value)
        this.query.value = query.copy(text = newText)
    }
}
