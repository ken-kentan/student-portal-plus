package jp.kentan.studentportalplus.ui.mycoursedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.MyCourseRepository
import jp.kentan.studentportalplus.ui.Event
import jp.kentan.studentportalplus.util.asLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyCourseDetailViewModel @Inject constructor(
    private val myCourseRepository: MyCourseRepository
) : ViewModel() {

    private val myCourseId = MutableLiveData<Long>()

    val myCourse = myCourseId.switchMap {
        myCourseRepository.getAsFlow(it).asLiveData()
    }

    val enabledDeleteOptionMenu = MediatorLiveData<Event<Unit>>().apply {
        addSource(myCourse) {
            if (it != null && it.isEditable) value = Event(Unit)
        }
    }.asLiveData()

    private val _error = MediatorLiveData<Event<Int>>().apply {
        addSource(myCourse) {
            if (it == null && !isDeleteMyCourse) {
                value = Event(R.string.all_not_found_error)
                _finish.value = Unit
            }
        }
    }
    val error: LiveData<Event<Int>>
        get() = _error

    private val _finish = MutableLiveData<Unit>()
    val finish: LiveData<Unit>
        get() = _finish

    private val _startEditMyCourseActivity = MutableLiveData<Event<Long>>()
    val startEditMyCourseActivity: LiveData<Event<Long>>
        get() = _startEditMyCourseActivity

    private val _showDeleteDialog = MutableLiveData<Event<String>>()
    val showDeleteDialog: LiveData<Event<String>>
        get() = _showDeleteDialog

    private var isDeleteMyCourse = false

    fun onActivityCreate(id: Long) {
        myCourseId.value = id
    }

    fun onEditClick() {
        val id = myCourseId.value ?: return
        _startEditMyCourseActivity.value = Event(id)
    }

    fun onDeleteClick() {
        val subject = myCourse.value?.subject ?: return
        _showDeleteDialog.value = Event(subject)
    }

    fun onDeleteConfirmClick() {
        val id = myCourseId.value ?: return

        viewModelScope.launch {
            isDeleteMyCourse = true

            if (myCourseRepository.remove(id)) {
                _finish.value = Unit
            } else {
                isDeleteMyCourse = false
                _error.value = Event(R.string.all_delete_failed)
            }
        }
    }
}
