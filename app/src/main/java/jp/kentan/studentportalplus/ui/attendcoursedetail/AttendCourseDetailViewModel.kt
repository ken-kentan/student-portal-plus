package jp.kentan.studentportalplus.ui.attendcoursedetail

import androidx.lifecycle.*
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.ui.Event
import jp.kentan.studentportalplus.util.asLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttendCourseDetailViewModel @Inject constructor(
    private val attendCourseRepository: AttendCourseRepository
) : ViewModel() {

    private val attendCourseId = MutableLiveData<Long>()

    val attendCourse = attendCourseId.switchMap {
        attendCourseRepository.getFlow(it).asLiveData()
    }

    val enabledDeleteOptionMenu = MediatorLiveData<Event<Unit>>().apply {
        addSource(attendCourse) {
            if (it?.type == AttendCourse.Type.USER) value = Event(Unit)
        }
    }.asLiveData()

    private val _error = MediatorLiveData<Event<Int>>().apply {
        addSource(attendCourse) {
            if (it == null && !isDeleteAttendCourse) {
                value = Event(R.string.error_not_found)
                _finish.value = Unit
            }
        }
    }
    val error: LiveData<Event<Int>>
        get() = _error

    private val _finish = MutableLiveData<Unit>()
    val finish: LiveData<Unit>
        get() = _finish

    private val _startEditAttendCourseActivity = MutableLiveData<Event<Long>>()
    val startEditAttendCourseActivity: LiveData<Event<Long>>
        get() = _startEditAttendCourseActivity

    private val _showDeleteDialog = MutableLiveData<Event<String>>()
    val showDeleteDialog: LiveData<Event<String>>
        get() = _showDeleteDialog

    private var isDeleteAttendCourse = false

    fun onActivityCreate(id: Long) {
        attendCourseId.value = id
    }

    fun onEditClick() {
        val id = attendCourseId.value ?: return
        _startEditAttendCourseActivity.value = Event(id)
    }

    fun onDeleteClick() {
        val subject = attendCourse.value?.subject ?: return
        _showDeleteDialog.value = Event(subject)
    }

    fun onDeleteConfirmClick() {
        val id = attendCourseId.value ?: return

        viewModelScope.launch {
            isDeleteAttendCourse = true

            if (attendCourseRepository.remove(id)) {
                _finish.value = Unit
            } else {
                isDeleteAttendCourse = false
                _error.value = Event(R.string.error_delete_failed)
            }
        }
    }
}