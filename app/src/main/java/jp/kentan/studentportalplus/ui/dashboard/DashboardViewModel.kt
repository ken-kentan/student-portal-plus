package jp.kentan.studentportalplus.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.ClassWeek
import jp.kentan.studentportalplus.data.component.PortalDataSet
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.ui.SingleLiveData
import java.util.*

class DashboardViewModel(
        private val portalRepository: PortalRepository
) : ViewModel() {

    companion object {
        const val MAX_ITEM_SIZE = 3
    }

    val portalDataSet: LiveData<PortalDataSet> = Transformations.map(portalRepository.portalDataSet) { set ->
        return@map PortalDataSet(
                myClassList = set.myClassList.toTodayTimetable(),
                lectureInfoList = set.lectureInfoList.filter { it.attend.isAttend() },
                lectureCancelList = set.lectureCancelList.filter { it.attend.isAttend() },
                noticeList = set.noticeList.take(MAX_ITEM_SIZE)
        )
    }

    val startMyClassDetailActivity = SingleLiveData<Long>()
    val startLectureInfoActivity = SingleLiveData<Long>()
    val startLectureCancelActivity = SingleLiveData<Long>()
    val startNoticeDetailActivity = SingleLiveData<Long>()

    fun onMyClassClick(id: Long) {
        startMyClassDetailActivity.value = id
    }

    fun onLectureInfoClick(id: Long) {
        startLectureInfoActivity.value = id
    }

    fun onLectureCancelClick(id: Long) {
        startLectureCancelActivity.value = id
    }

    fun onNoticeItemClick(id: Long) {
        startNoticeDetailActivity.value = id
    }

    fun onNoticeFavoriteClick(data: Notice) {
        portalRepository.updateNotice(data.copy(isFavorite = !data.isFavorite))
    }

    private fun List<MyClass>.toTodayTimetable(): List<MyClass> {
        val calender = Calendar.getInstance()

        val hour = calender.get(Calendar.HOUR_OF_DAY)
        var dayOfWeek = calender.get(Calendar.DAY_OF_WEEK)

        // 午後8時以降は明日の時間割
        if (hour >= 20) { dayOfWeek++ }

        // 土、日は月に
        val week = if (dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY) ClassWeek.valueOf(dayOfWeek-1) else ClassWeek.MONDAY

        return filter { it.week == week }
    }
}
