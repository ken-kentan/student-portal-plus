package jp.kentan.studentportalplus.ui.lectureinformation

import android.util.Log
import androidx.lifecycle.*
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class LectureInformationViewModel @Inject constructor(
    private val lectureInfoRepository: LectureInformationRepository
) : ViewModel() {

    //    val lectureInfoList = lectureInfoRepository.getListFlow().asLiveData()

    private val _searchQuery = MutableLiveData(LectureQuery())
    val searchQueryText: String?
        get() = _searchQuery.value?.text

    val lectureInfoList: LiveData<List<LectureInformation>> = _searchQuery.switchMap { query ->
        lectureInfoRepository.getListFlow(query).asLiveData()
    }

    private val _startDetailActivity = MutableLiveData<Event<Long>>()
    val startDetailActivity: LiveData<Event<Long>>
        get() = _startDetailActivity

    val onItemClick = { id: Long ->
        _startDetailActivity.value = Event(id)
    }

    fun onQueryTextChange(newText: String?) {
        val query = requireNotNull(_searchQuery.value)
        _searchQuery.value = query.copy(text = newText)
        Log.d("LectureInformationVM", "onQueryTextChange: $newText")
    }
}
