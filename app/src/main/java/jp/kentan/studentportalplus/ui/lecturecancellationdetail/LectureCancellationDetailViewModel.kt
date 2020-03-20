package jp.kentan.studentportalplus.ui.lecturecancellationdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.ui.Event
import jp.kentan.studentportalplus.util.asLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class LectureCancellationDetailViewModel @Inject constructor(
    private val lectureCancelRepository: LectureCancellationRepository,
    private val attendCourseRepository: AttendCourseRepository
) : ViewModel() {

    private val lectureCancelId = MutableLiveData<Long>()

    val lectureCancel = lectureCancelId.switchMap {
        lectureCancelRepository.getFlow(it).asLiveData()
    }

    private val _excludeFromAttendConfirmDialog = MutableLiveData<Event<String>>()
    val excludeFromAttendConfirmDialog: LiveData<Event<String>>
        get() = _excludeFromAttendConfirmDialog

    private val _snackbar = MutableLiveData<Event<Int>>()
    val snackbar: LiveData<Event<Int>>
        get() = _snackbar

    private val _indefiniteSnackbar = MutableLiveData<Event<Int>>()
    val indefiniteSnackbar: LiveData<Event<Int>>
        get() = _indefiniteSnackbar

    val finishWithNotFoundError = MediatorLiveData<Unit>().apply {
        addSource(lectureCancel) {
            if (it == null) value = Unit
        }
    }.asLiveData()

    fun onActivityCreate(id: Long) {
        viewModelScope.launch {
            lectureCancelRepository.setRead(id)
        }

        lectureCancelId.value = id
    }

    fun onCourseAttendFabClick() {
        val lectureCancel = lectureCancel.value ?: return

        if (lectureCancel.attendType.canAttend) {
            viewModelScope.launch {
                val isSuccess = attendCourseRepository.add(lectureCancel)

                if (isSuccess) {
                    _snackbar.value = Event(R.string.all_add_to_attend_course)
                } else {
                    _indefiniteSnackbar.value = Event(R.string.all_update_failed)
                }
            }
        } else if (lectureCancel.attendType != AttendCourse.Type.PORTAL) {
            _excludeFromAttendConfirmDialog.value = Event(lectureCancel.subject)
        }
    }

    fun onExcludeConfirmClick(subject: String) {
        viewModelScope.launch {
            val isSuccess = attendCourseRepository.remove(subject)

            if (isSuccess) {
                _snackbar.value = Event(R.string.all_exclude_from_attend_course)
            } else {
                _indefiniteSnackbar.value = Event(R.string.all_update_failed)
            }
        }
    }
}
