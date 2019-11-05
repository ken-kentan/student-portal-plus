package jp.kentan.studentportalplus.ui.lectureinformationdetail

import androidx.lifecycle.*
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.ui.Event
import jp.kentan.studentportalplus.util.asLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class LectureInformationDetailViewModel @Inject constructor(
    private val lectureInfoRepository: LectureInformationRepository,
    private val attendCourseRepository: AttendCourseRepository
) : ViewModel() {

    private val lectureInfoId = MutableLiveData<Long>()

    val lectureInfo = lectureInfoId.switchMap {
        lectureInfoRepository.getFlow(it).asLiveData()
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
        addSource(lectureInfo) {
            if (it == null) value = Unit
        }
    }.asLiveData()

    fun onActivityCreate(id: Long) {
        viewModelScope.launch {
            lectureInfoRepository.setRead(id)
        }

        lectureInfoId.value = id
    }

    fun onCourseAttendFabClick() {
        val lectureInfo = lectureInfo.value ?: return

        if (lectureInfo.attendType.canAttend) {
            viewModelScope.launch {
                val isSuccess = attendCourseRepository.add(lectureInfo)

                if (isSuccess) {
                    _snackbar.value = Event(R.string.text_add_to_attend_course)
                } else {
                    _indefiniteSnackbar.value = Event(R.string.error_update_failed)
                }
            }
        } else if (lectureInfo.attendType != AttendCourse.Type.PORTAL) {
            _excludeFromAttendConfirmDialog.value = Event(lectureInfo.subject)
        }
    }

    fun onExcludeConfirmClick(subject: String) {
        viewModelScope.launch {
            val isSuccess = attendCourseRepository.remove(subject)

            if (isSuccess) {
                _snackbar.value = Event(R.string.text_exclude_from_attend_course)
            } else {
                _indefiniteSnackbar.value = Event(R.string.error_update_failed)
            }
        }
    }
}