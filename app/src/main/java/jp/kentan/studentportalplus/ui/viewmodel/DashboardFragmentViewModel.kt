package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.component.PortalDataSet
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.data.model.Notice
import org.jetbrains.anko.coroutines.experimental.bg
import java.util.*


class DashboardFragmentViewModel(private val repository: PortalRepository) : ViewModel() {

    fun getMyClassList(): LiveData<Pair<String, List<MyClass>>> =
            Transformations.map(repository.myClassList) { toTodayTimetable(it) }

    fun getAttendLectureInformationList(): LiveData<List<LectureInformation>> =
            Transformations.map(repository.lectureInformationList) { it.filter { it.attend.isAttend() } }

    fun getAttendLectureCancellationList(): LiveData<List<LectureCancellation>> =
            Transformations.map(repository.lectureCancellationList) { it.filter { it.attend.isAttend() } }

    fun getNoticeList(): LiveData<List<Notice>> = repository.noticeList

    fun getResults(): LiveData<PortalDataSet> = Transformations.map(repository.portalDataSet) {
        it.copy(
                myClassList = toTodayTimetable(it.myClassList).second,
                lectureInfoList = it.lectureInfoList.filter { it.attend.isAttend() },
                lectureCancelList = it.lectureCancelList.filter { it.attend.isAttend() }
        )
    }

    fun updateLectureInformation(data: LectureInformation) = bg {
        repository.update(data)
    }

    fun updateLectureCancellation(data: LectureCancellation) = bg {
        repository.update(data)
    }

    fun updateNotice(data: Notice) = bg {
        repository.update(data)
    }

    private fun toTodayTimetable(list: List<MyClass>): Pair<String, List<MyClass>> {
        val hour      = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        var dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        // 午後8時以降は明日の時間割
        if (hour >= 20) {
            dayOfWeek++
        }

        //土、日は月に
        val week = if (dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY) ClassWeekType.valueOf(dayOfWeek-1) else ClassWeekType.MONDAY

        return Pair(week.fullDisplayName, list.filter { it.week == week })
    }
}