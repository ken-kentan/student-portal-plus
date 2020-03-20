package jp.kentan.studentportalplus.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.ui.Event
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    attendCourseRepository: AttendCourseRepository,
    lectureInfoRepository: LectureInformationRepository,
    lectureCancelRepository: LectureCancellationRepository,
    noticeRepository: NoticeRepository
) : ViewModel() {

    private val _portalSet = PortalSetLiveData()
    val portalSet: LiveData<PortalSet>
        get() = _portalSet

    private val _navigate = MutableLiveData<Event<Int>>()
    val navigate: LiveData<Event<Int>>
        get() = _navigate

    private val _startAttendCourseDetailActivity = MutableLiveData<Event<Long>>()
    val startAttendCourseDetailActivity: LiveData<Event<Long>>
        get() = _startAttendCourseDetailActivity

    private val _startLectureInfoDetailActivity = MutableLiveData<Event<Long>>()
    val startLectureInfoActivity: LiveData<Event<Long>>
        get() = _startLectureInfoDetailActivity

    private val _startLectureCancelDetailActivity = MutableLiveData<Event<Long>>()
    val startLectureCancelActivity: LiveData<Event<Long>>
        get() = _startLectureCancelDetailActivity

    private val _startNoticeDetailActivity = MutableLiveData<Event<Long>>()
    val startNoticeActivity: LiveData<Event<Long>>
        get() = _startNoticeDetailActivity

    init {
        val today = Calendar.getInstance()

        var dayOfWeek = today.get(Calendar.DAY_OF_WEEK)

        // 午後8時以降は明日の時間割
        if (today.get(Calendar.HOUR_OF_DAY) >= 20) {
            dayOfWeek++
        }

        val timetableDayOfWeek = when (dayOfWeek) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            else -> DayOfWeek.MONDAY
        }

        _portalSet.addAttendCourseSource(
            attendCourseRepository.getListFlow(timetableDayOfWeek).asLiveData()
        )
        _portalSet.addLectureInformationSource(
            lectureInfoRepository.getListFlow().map { list ->
                list.filter { it.attendType.isAttend }
            }.asLiveData()
        )
        _portalSet.addLectureCancellationSource(
            lectureCancelRepository.getListFlow().map { list ->
                list.filter { it.attendType.isAttend }
            }.asLiveData()
        )
        _portalSet.addNoticeSource(noticeRepository.getListFlow().asLiveData())
    }

    val onAttendCourseItemClick = { id: Long ->
        _startAttendCourseDetailActivity.value = Event(id)
    }

    val onLectureInformationItemClick = { id: Long ->
        _startLectureInfoDetailActivity.value = Event(id)
    }

    val onLectureCancellationItemClick = { id: Long ->
        _startLectureCancelDetailActivity.value = Event(id)
    }

    val onNoticeItemClick = { id: Long ->
        _startNoticeDetailActivity.value = Event(id)
    }

    val onNoticeFavoriteClick: (Notice) -> Unit = { notice: Notice ->
        viewModelScope.launch {
            noticeRepository.update(notice.copy(isFavorite = !notice.isFavorite))
        }
    }

    val onLectureInformationShowAllClick = {
        _navigate.value = Event(R.id.lecture_informations_fragment)
    }

    val onLectureCancellationShowAllClick = {
        _navigate.value = Event(R.id.lecture_cancellations_fragment)
    }

    val onNoticeShowAllClick = {
        _navigate.value = Event(R.id.notices_fragment)
    }
}
