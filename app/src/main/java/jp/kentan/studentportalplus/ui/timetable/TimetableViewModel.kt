package jp.kentan.studentportalplus.ui.timetable

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.MyCourseRepository
import jp.kentan.studentportalplus.data.Preferences
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period
import jp.kentan.studentportalplus.ui.Event
import java.util.Calendar
import javax.inject.Inject

class TimetableViewModel @Inject constructor(
    myCourseRepository: MyCourseRepository,
    private val preferences: Preferences
) : ViewModel() {

    val myCourseList = myCourseRepository.getAllAsFlow().asLiveData()

    private val _isGridLayout = MutableLiveData(preferences.isGridTimetableLayout)
    val isGridLayout: LiveData<Boolean>
        get() = _isGridLayout

    private val _dayOfWeek = MutableLiveData(DayOfWeek.MONDAY)
    val dayOfWeek: LiveData<DayOfWeek>
        get() = _dayOfWeek

    private val _startEditMyCourseActivity = MutableLiveData<Event<Pair<Period, DayOfWeek>>>()
    val startEditMyCourseActivity: LiveData<Event<Pair<Period, DayOfWeek>>>
        get() = _startEditMyCourseActivity

    private val _startMyCourseDetailActivity = MutableLiveData<Event<Long>>()
    val startMyCourseDetailActivity: LiveData<Event<Long>>
        get() = _startMyCourseDetailActivity

    val onMyCourseClick = { id: Long ->
        _startMyCourseDetailActivity.value = Event(id)
    }

    val onBlankClick = { period: Period, dayOfWeek: DayOfWeek ->
        _startEditMyCourseActivity.value = Event(Pair(period, dayOfWeek))
    }

    fun onResume() {
        _dayOfWeek.value = when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            else -> DayOfWeek.UNKNOWN
        }
    }

    fun onAddClick() {
        _startEditMyCourseActivity.value = Event(Pair(Period.ONE, DayOfWeek.MONDAY))
        R.style.TextAppearance_MaterialComponents_Body1
    }

    fun onSwitchLayoutClick() {
        val isGrid = requireNotNull(_isGridLayout.value)

        _isGridLayout.value = !isGrid
        preferences.isGridTimetableLayout = !isGrid
    }
}
