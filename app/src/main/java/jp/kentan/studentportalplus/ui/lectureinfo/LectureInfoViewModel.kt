package jp.kentan.studentportalplus.ui.lectureinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.ui.SingleLiveData

class LectureInfoViewModel(
        private val portalRepository: PortalRepository
) : ViewModel() {

    private val queryLiveData = MutableLiveData<LectureQuery>()
    var query = LectureQuery()
        private set

    val lectureInfoList: LiveData<List<LectureInformation>> = Transformations.switchMap(queryLiveData) {
        portalRepository.getLectureInfoList(it)
    }

    val startDetailActivity = SingleLiveData<Long>()

    init {
        queryLiveData.value = query
    }

    fun onClick(id: Long) {
        startDetailActivity.value = id
    }

    fun onQueryTextChange(text: String?) {
        query = query.copy(keyword = text)

        queryLiveData.value = query
    }

    fun onFilterApplyClick(
            order: LectureQuery.Order,
            isUnread: Boolean,
            isRead: Boolean,
            isAttend: Boolean
    ) {
        query = query.copy(
                order = order,
                isUnread = isUnread,
                isRead = isRead,
                isAttend = isAttend
        )

        queryLiveData.value = query
    }
}
