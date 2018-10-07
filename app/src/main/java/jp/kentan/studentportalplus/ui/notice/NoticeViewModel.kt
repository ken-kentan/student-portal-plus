package jp.kentan.studentportalplus.ui.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.NoticeQuery
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.ui.SingleLiveData

class NoticeViewModel(
        private val portalRepository: PortalRepository
) : ViewModel() {

    var query = NoticeQuery()
        private set

    private val queryLiveData = MutableLiveData<NoticeQuery>()

    val noticeList: LiveData<List<Notice>> = Transformations.switchMap(queryLiveData) {
        portalRepository.getNoticeList(it)
    }
    val startDetailActivity = SingleLiveData<Long>()

    init {
        queryLiveData.value = query
    }

    fun onClick(id: Long) {
        startDetailActivity.value = id
    }

    fun onFavoriteClick(data: Notice) {
        portalRepository.updateNotice(data.copy(isFavorite = !data.isFavorite))
    }

    fun onQueryTextChange(text: String?) {
        query = query.copy(keyword = text)

        queryLiveData .value = query
    }

    fun onFilterApplyClick(
            range: NoticeQuery.DateRange,
            isUnread: Boolean,
            isRead: Boolean,
            isFavorite: Boolean
    ) {
        query = query.copy(
                dateRange = range,
                isUnread = isUnread,
                isRead = isRead,
                isFavorite = isFavorite
        )

        queryLiveData .value = query
    }
}
