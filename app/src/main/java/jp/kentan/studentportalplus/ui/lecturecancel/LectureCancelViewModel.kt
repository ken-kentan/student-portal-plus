package jp.kentan.studentportalplus.ui.lecturecancel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.ui.SingleLiveData
import jp.kentan.studentportalplus.util.getLectureCancelOrder
import jp.kentan.studentportalplus.util.setLectureCancelOrder

class LectureCancelViewModel(
        private val preferences: SharedPreferences,
        private val portalRepository: PortalRepository
) : ViewModel() {


    private val queryLiveData = MutableLiveData<LectureQuery>()
    var query = LectureQuery(order = preferences.getLectureCancelOrder())
        private set(value) {
            field = value
            queryLiveData.value = query

            preferences.setLectureCancelOrder(value.order)
        }

    val lectureCancelList: LiveData<List<LectureCancellation>> = queryLiveData.switchMap {
        portalRepository.getLectureCancelList(it)
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
    }
}
