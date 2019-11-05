package jp.kentan.studentportalplus.ui.editattendcourse

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period
import jp.kentan.studentportalplus.ui.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditAttendCourseViewModel @Inject constructor(
    application: Application,
    private val attendCourseRepository: AttendCourseRepository
) : AndroidViewModel(application) {

    val dayOfWeekList = DayOfWeek.values().map { it.format(application) }
    val periodList = Period.values().map { it.format(application) }

    val color = MutableLiveData<AttendCourse.Color>()
    val subject = MutableLiveData<String>()
    val instructor = MutableLiveData<String>()
    val location = MutableLiveData<String>()
    val dayOfWeek = MutableLiveData<String>()
    val period = MutableLiveData<String>()
    val category = MutableLiveData<String>()
    val credit = MutableLiveData<String>()
    val scheduleCode = MutableLiveData<String>()

    private val _errorSubject = MediatorLiveData<Int>().apply {
        addSource(subject) { value = null }
    }
    val errorSubject: LiveData<Int>
        get() = _errorSubject

    private val _errorCredit = MediatorLiveData<Int>().apply {
        addSource(credit) { value = null }
    }
    val errorCredit: LiveData<Int>
        get() = _errorCredit

    private val _errorScheduleCode = MediatorLiveData<Int>().apply {
        addSource(scheduleCode) { value = null }
    }
    val errorScheduleCode: LiveData<Int>
        get() = _errorScheduleCode

    val isEnabledPeriod: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(dayOfWeek) { displayName ->
            value = displayName.toDayOfWeekEnum().hasPeriod
        }
    }

    private val _isEnabled = MutableLiveData<Boolean>(false)
    val isEnabled: LiveData<Boolean>
        get() = _isEnabled

    private val _toast = MutableLiveData<Event<Int>>()
    val toast: LiveData<Event<Int>>
        get() = _toast

    private val _showFinishConfirmDialog = MutableLiveData<Event<Unit>>()
    val showFinishConfirmDialog: LiveData<Event<Unit>>
        get() = _showFinishConfirmDialog

    private val _finish = MutableLiveData<Unit>()
    val finish: LiveData<Unit>
        get() = _finish

    private lateinit var editAttendCourseMode: EditAttendCourseMode

    private lateinit var originalAttendCourse: AttendCourse

    fun onActivityCreate(mode: EditAttendCourseMode) {
        editAttendCourseMode = mode

        when (mode) {
            is EditAttendCourseMode.Update -> {
                viewModelScope.launch {
                    val course = attendCourseRepository.get(mode.id) ?: let {
                        _toast.value = Event(R.string.error_not_found)
                        _finish.value = Unit
                        return@launch
                    }

                    setAttendCourse(course)
                }
            }
            is EditAttendCourseMode.Add -> setAttendCourse(
                AttendCourse(
                    subject = "",
                    instructor = "",
                    dayOfWeek = mode.dayOfWeek,
                    period = mode.period.value,
                    category = "",
                    credit = 0,
                    scheduleCode = "",
                    type = AttendCourse.Type.USER
                )
            )
        }
    }

    private fun setAttendCourse(course: AttendCourse) {
        originalAttendCourse = course

        val context: Context = getApplication()

        color.value = course.color
        subject.value = course.subject
        instructor.value = course.instructor
        location.value = course.location
        dayOfWeek.value = course.dayOfWeek.format(context)
        period.value = if (course.period > 0) periodList[course.period - 1] else ""
        category.value = course.category
        credit.value = with(course.credit) { if (this > 0) toString() else "" }
        scheduleCode.value = course.scheduleCode

        _isEnabled.value = course.type == AttendCourse.Type.USER
    }

    fun onSaveClick() {
        val subject = subject.value.orEmpty()
        val credit = credit.value?.toIntOrNull()
        val scheduleCode = scheduleCode.value.orEmpty()

        var isCancel = false

        if (subject.isBlank()) {
            _errorSubject.value = R.string.error_field_empty
            isCancel = true
        }
        if (credit != null && credit !in 1..10) {
            _errorCredit.value = R.string.error_invalid_credit
            isCancel = true
        }
        if (scheduleCode.validateScheduleCode()) {
            _errorScheduleCode.value = R.string.error_invalid_schedule_code
            isCancel = true
        }

        if (isCancel) {
            return
        }

        val location: String? = with(location.value) { if (isNullOrBlank()) null else this }
        val dayOfWeek = dayOfWeek.value.toDayOfWeekEnum()

        val course = AttendCourse(
            id = originalAttendCourse.id,
            subject = subject,
            instructor = instructor.value.orEmpty(),
            location = location,
            dayOfWeek = dayOfWeek,
            period = if (dayOfWeek.hasPeriod) period.value.toPeriodValue() else 0,
            category = category.value.orEmpty(),
            credit = credit ?: 0,
            scheduleCode = scheduleCode,
            type = originalAttendCourse.type
        )

        viewModelScope.launch {
            val isSuccess = when (editAttendCourseMode) {
                is EditAttendCourseMode.Update -> attendCourseRepository.update(course)
                is EditAttendCourseMode.Add -> attendCourseRepository.add(course)
            }

            if (isSuccess) {
                _finish.value = Unit
            } else {
                _toast.value = Event(R.string.error_save_failed)
            }
        }
    }

    fun onFinish() {
        val context: Context = getApplication()

        val original = originalAttendCourse
        val originalCredit = with(original.credit) { if (this > 0) toString() else "" }

        val location = with(location.value) { if (isNullOrEmpty()) null else this }
        val period = period.value?.toPeriodValue()

        val isSameWithOriginal = (color.value == original.color) &&
            (subject.value == original.subject) &&
            (instructor.value == original.instructor) &&
            (location == original.location) &&
            (dayOfWeek.value == original.dayOfWeek.format(context)) &&
            ((period == original.period || !original.dayOfWeek.hasPeriod)) &&
            (category.value == original.category) &&
            (credit.value == originalCredit) &&
            (scheduleCode.value == original.scheduleCode)

        if (isSameWithOriginal) {
            _finish.value = Unit
        } else {
            _showFinishConfirmDialog.value = Event(Unit)
        }
    }

    private fun DayOfWeek.format(context: Context) = if (hasSuffix) {
        context.getString(R.string.suffix_day_of_week, context.getString(resId))
    } else {
        context.getString(resId)
    }

    private fun Period.format(context: Context) = context.getString(R.string.suffix_period, value)

    private fun String?.toDayOfWeekEnum(): DayOfWeek {
        val displayName = this ?: return DayOfWeek.UNKNOWN
        val index = dayOfWeekList.indexOf(displayName)

        return DayOfWeek.values()[index]
    }

    private fun String?.toPeriodValue(): Int {
        if (isNullOrBlank()) {
            return 0
        }

        val index = periodList.indexOf(this)

        return Period.values()[index].value
    }

    private fun String.validateScheduleCode(): Boolean {
        val code = toIntOrNull() ?: return !isEmpty()
        return code !in 10000000..1000000000
    }
}