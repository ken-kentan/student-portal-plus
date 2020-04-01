package jp.kentan.studentportalplus.ui.lectureinformationdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.MyCourseRepository
import jp.kentan.studentportalplus.data.vo.MyCourseType
import jp.kentan.studentportalplus.ui.Event
import jp.kentan.studentportalplus.util.asLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class LectureInformationDetailViewModel @Inject constructor(
    private val lectureInfoRepository: LectureInformationRepository,
    private val myCourseRepository: MyCourseRepository
) : ViewModel() {

    private val lectureInfoId = MutableLiveData<Long>()

    val lectureInfo = lectureInfoId.switchMap {
        lectureInfoRepository.getAsFlow(it).asLiveData()
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

    fun onMyCourseFabClick() {
        val lectureInfo = lectureInfo.value ?: return

        if (lectureInfo.myCourseType.canAddToMyCourse) {
            viewModelScope.launch {
                val isSuccess = myCourseRepository.add(lectureInfo)

                if (isSuccess) {
                    _snackbar.value = Event(R.string.all_add_to_my_course)
                } else {
                    _indefiniteSnackbar.value = Event(R.string.all_update_failed)
                }
            }
        } else if (lectureInfo.myCourseType == MyCourseType.EDITABLE) {
            _excludeFromMyConfirmDialog.value = Event(lectureInfo.subject)
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
