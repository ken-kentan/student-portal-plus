package jp.kentan.studentportalplus.ui.timetable

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period
import jp.kentan.studentportalplus.ui.Event
import javax.inject.Inject

class TimetableViewModel @Inject constructor(
    attendCourseRepository: AttendCourseRepository,
    private val localPreferences: LocalPreferences
) : ViewModel() {

    val attendCourseList = attendCourseRepository.getListFlow().asLiveData()

    private val _isGridLayout = MutableLiveData<Boolean>(localPreferences.isGridTimetableLayout)
    val isGridLayout: LiveData<Boolean>
        get() = _isGridLayout

    private val _startEditAttendCourseActivity = MutableLiveData<Event<Pair<Period, DayOfWeek>>>()
    val startEditAttendCourseActivity: LiveData<Event<Pair<Period, DayOfWeek>>>
        get() = _startEditAttendCourseActivity

    private val _startAttendCourseDetailActivity = MutableLiveData<Event<Long>>()
    val startAttendCourseDetailActivity: LiveData<Event<Long>>
        get() = _startAttendCourseDetailActivity

    val onAttendCourseClick = { id: Long ->
        _startAttendCourseDetailActivity.value = Event(id)
    }

    val onBlankClick = { period: Period, dayOfWeek: DayOfWeek ->
        _startEditAttendCourseActivity.value = Event(Pair(period, dayOfWeek))
    }

    fun onAddClick() {
        _startEditAttendCourseActivity.value = Event(Pair(Period.ONE, DayOfWeek.MONDAY))
    }

    fun onSwitchLayoutClick() {
        val isGrid = requireNotNull(_isGridLayout.value)

        _isGridLayout.value = !isGrid
        localPreferences.isGridTimetableLayout = !isGrid
    }
}
