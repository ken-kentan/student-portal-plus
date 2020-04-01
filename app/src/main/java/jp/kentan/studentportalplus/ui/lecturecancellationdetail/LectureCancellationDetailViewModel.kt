package jp.kentan.studentportalplus.ui.lecturecancellationdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.MyCourseRepository
import jp.kentan.studentportalplus.data.vo.MyCourseType
import jp.kentan.studentportalplus.ui.Event
import jp.kentan.studentportalplus.util.asLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class LectureCancellationDetailViewModel @Inject constructor(
    private val lectureCancelRepository: LectureCancellationRepository,
    private val myCourseRepository: MyCourseRepository
) : ViewModel() {

    private val lectureCancelId = MutableLiveData<Long>()

    val lectureCancel = lectureCancelId.switchMap {
        lectureCancelRepository.getAsFlow(it).asLiveData()
    }

    private val _excludeFromMyConfirmDialog = MutableLiveData<Event<String>>()
    val excludeFromMyConfirmDialog: LiveData<Event<String>>
        get() = _excludeFromMyConfirmDialog

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

    fun onMyCourseFabClick() {
        val lectureCancel = lectureCancel.value ?: return

        if (lectureCancel.myCourseType.canAddToMyCourse) {
            viewModelScope.launch {
                val isSuccess = myCourseRepository.add(lectureCancel)

                if (isSuccess) {
                    _snackbar.value = Event(R.string.all_add_to_my_course)
                } else {
                    _indefiniteSnackbar.value = Event(R.string.all_update_failed)
                }
            }
        } else if (lectureCancel.myCourseType == MyCourseType.EDITABLE) {
            _excludeFromMyConfirmDialog.value = Event(lectureCancel.subject)
        }
    }

    fun onExcludeConfirmClick(subject: String) {
        viewModelScope.launch {
            val isSuccess = myCourseRepository.remove(subject)

            if (isSuccess) {
                _snackbar.value = Event(R.string.all_exclude_from_my_course)
            } else {
                _indefiniteSnackbar.value = Event(R.string.all_update_failed)
            }
        }
    }
}
