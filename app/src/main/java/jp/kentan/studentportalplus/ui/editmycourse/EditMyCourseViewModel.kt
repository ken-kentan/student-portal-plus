package jp.kentan.studentportalplus.ui.editmycourse

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.MyCourseRepository
import jp.kentan.studentportalplus.data.SubjectRepository
import jp.kentan.studentportalplus.data.entity.MyCourse
import jp.kentan.studentportalplus.data.vo.CourseColor
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period
import jp.kentan.studentportalplus.ui.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditMyCourseViewModel @Inject constructor(
    application: Application,
    private val myCourseRepository: MyCourseRepository,
    subjectRepository: SubjectRepository
) : AndroidViewModel(application) {

    val subjectList = subjectRepository.getAllAsFlow().asLiveData()

    val dayOfWeekList = DayOfWeek.values().map { it.format(application) }
    val periodList = Period.values().map { it.format(application) }

    val color = MutableLiveData<CourseColor>()
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

    private val _isEnabled = MutableLiveData(false)
    val isEnabled: LiveData<Boolean>
        get() = _isEnabled

    private val _toast = MutableLiveData<Event<Int>>()
    val toast: LiveData<Event<Int>>
        get() = _toast

    private val _showColorPickerDialog = MutableLiveData<Event<CourseColor>>()
    val showColorPickerDialog: LiveData<Event<CourseColor>>
        get() = _showColorPickerDialog

    private val _showFinishConfirmDialog = MutableLiveData<Event<Unit>>()
    val showFinishConfirmDialog: LiveData<Event<Unit>>
        get() = _showFinishConfirmDialog

    private val _finish = MutableLiveData<Unit>()
    val finish: LiveData<Unit>
        get() = _finish

    private lateinit var editMyCourseMode: EditMyCourseMode

    private lateinit var originalMyCourse: MyCourse

    fun onActivityCreate(mode: EditMyCourseMode) {
        editMyCourseMode = mode

        when (mode) {
            is EditMyCourseMode.Update -> {
                viewModelScope.launch {
                    val course = myCourseRepository.get(mode.id) ?: let {
                        _toast.value = Event(R.string.all_not_found_error)
                        _finish.value = Unit
                        return@launch
                    }

                    setMyCourse(course)
                }
            }
            is EditMyCourseMode.Add -> setMyCourse(
                MyCourse(
                    subject = "",
                    instructor = "",
                    dayOfWeek = mode.dayOfWeek,
                    period = mode.period.value,
                    category = "",
                    credit = 0,
                    scheduleCode = "",
                    isEditable = true
                )
            )
        }
    }

    private fun setMyCourse(course: MyCourse) {
        originalMyCourse = course

        val context: Context = getApplication()

        color.value = course.color
        subject.value = course.subject
        instructor.value = course.instructor
        location.value = course.location
        dayOfWeek.value = course.dayOfWeek.format(context)
        period.value = periodList[course.period - 1]
        category.value = course.category
        credit.value = with(course.credit) { if (this > 0) toString() else "" }
        scheduleCode.value = course.scheduleCode

        _isEnabled.value = course.isEditable
    }

    fun onSaveClick() {
        val subject = subject.value.orEmpty()
        val credit = credit.value?.toIntOrNull()
        val scheduleCode = scheduleCode.value.orEmpty()

        var isCancel = false

        if (subject.isBlank()) {
            _errorSubject.value = R.string.all_field_empty
            isCancel = true
        }
        if (credit != null && credit !in 1..10) {
            _errorCredit.value = R.string.edit_my_course_invalid_credit
            isCancel = true
        }
        if (scheduleCode.validateScheduleCode()) {
            _errorScheduleCode.value = R.string.edit_my_course_invalid_schedule_code
            isCancel = true
        }

        if (isCancel) {
            return
        }

        val location: String? = with(location.value) { if (isNullOrBlank()) null else this }
        val dayOfWeek = dayOfWeek.value.toDayOfWeekEnum()

        val course = MyCourse(
            id = originalMyCourse.id,
            subject = subject,
            instructor = instructor.value.orEmpty(),
            location = location,
            dayOfWeek = dayOfWeek,
            period = if (dayOfWeek.hasPeriod) period.value.toPeriodValue() else 1,
            category = category.value.orEmpty(),
            credit = credit ?: 0,
            scheduleCode = scheduleCode,
            color = requireNotNull(color.value),
            isEditable = originalMyCourse.isEditable
        )

        viewModelScope.launch {
            val isSuccess = when (editMyCourseMode) {
                is EditMyCourseMode.Update -> myCourseRepository.update(course)
                is EditMyCourseMode.Add -> myCourseRepository.add(course)
            }

            if (isSuccess) {
                _finish.value = Unit
            } else {
                _toast.value = Event(R.string.edit_my_course_save_failed)
            }
        }
    }

    fun onColorClick() {
        val color = color.value ?: return
        _showColorPickerDialog.value = Event(color)
    }

    fun onCourseColorSelect(courseColor: CourseColor) {
        color.value = courseColor
    }

    fun onFinish() {
        val context: Context = getApplication()

        val original = originalMyCourse
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
        context.getString(R.string.edit_my_course_day_of_week_suffix, context.getString(resId))
    } else {
        context.getString(resId)
    }

    private fun Period.format(context: Context) =
        context.getString(R.string.all_period_suffix, value)

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
