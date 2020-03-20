package jp.kentan.studentportalplus.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.data.source.ShibbolethException
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    application: Application,
    userRepository: UserRepository,
    private val attendCourseRepository: AttendCourseRepository,
    private val lectureInfoRepository: LectureInformationRepository,
    private val lectureCancelRepository: LectureCancellationRepository,
    private val noticeRepository: NoticeRepository,
    private val localPreferences: LocalPreferences
) : AndroidViewModel(application) {

    val user = userRepository.getFlow().asLiveData()
    val isSyncing = MutableLiveData<Boolean>()

    private val _errorSnackbar = MutableLiveData<Event<String>>()
    val errorSnackbar: LiveData<Event<String>>
        get() = _errorSnackbar

    private val _loginSnackbar = MutableLiveData<Event<Unit>>()
    val loginSnackbar: LiveData<Event<Unit>>
        get() = _loginSnackbar

    private val _closeDrawer = MutableLiveData<Event<Unit>>()
    val closeDrawer: LiveData<Event<Unit>>
        get() = _closeDrawer

    val shouldLaunchWelcomeActivity: Boolean
        get() = !localPreferences.isAuthenticatedUser

    fun onCreate(shouldRefresh: Boolean) {
        if (shouldRefresh) {
            onRefresh()
        }
    }

    fun onRefresh() {
        if (isSyncing.value == true) {
            return
        }

        isSyncing.value = true

        viewModelScope.launch {
            runCatching {
                attendCourseRepository.syncWithRemote()
                lectureInfoRepository.syncWithRemote()
                lectureCancelRepository.syncWithRemote()
                noticeRepository.syncWithRemote()
            }.onFailure {
                if (it is ShibbolethException) {
                    _loginSnackbar.value = Event(Unit)
                    return@onFailure
                }

                val throwableMessage = it.message
                val message =
                    if (localPreferences.isEnabledDetailError && !throwableMessage.isNullOrEmpty()) {
                        throwableMessage
                    } else {
                        getApplication<Application>().getString(R.string.main_sync_failed_error)
                    }

                _errorSnackbar.value = Event(message)
            }.also {
                isSyncing.value = false
            }
        }
    }
}
