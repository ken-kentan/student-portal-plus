package jp.kentan.studentportalplus.ui.notices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.data.vo.NoticeQuery
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class NoticesViewModel @Inject constructor(
    noticeRepository: NoticeRepository
) : ViewModel() {

    private val _query = MutableLiveData(NoticeQuery())
    val query: NoticeQuery
        get() = requireNotNull(_query.value)

    val queryText: String?
        get() = query.text

    val lectureInfoList = noticeRepository.getAllAsFlow(_query.asFlow()).asLiveData()

    private val _startDetailActivity = MutableLiveData<Event<Long>>()
    val startDetailActivity: LiveData<Event<Long>>
        get() = _startDetailActivity

    val onItemClick = { id: Long ->
        _startDetailActivity.value = Event(id)
    }

    fun onQueryTextChange(newText: String?) {
        _query.value = query.copy(text = newText)
    }

    fun onFilterApplyClick(query: NoticeQuery) {
        _query.value = query
    }
}
