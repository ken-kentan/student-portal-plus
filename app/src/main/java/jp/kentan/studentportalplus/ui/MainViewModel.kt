package jp.kentan.studentportalplus.ui

import android.app.Application
import androidx.lifecycle.*
import androidx.work.WorkManager
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.*
import jp.kentan.studentportalplus.data.source.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.work.sync.SyncWorker
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

    private val _indefiniteSnackbar = MutableLiveData<Event<String>>()
    val indefiniteSnackbar: LiveData<Event<String>>
        get() = _indefiniteSnackbar

    private val _closeDrawer = MutableLiveData<Event<Unit>>()
    val closeDrawer: LiveData<Event<Unit>>
        get() = _closeDrawer

    fun onRefresh() {
        val request = SyncWorker.buildOneTimeWorkRequest()
        WorkManager.getInstance(getApplication())
            .enqueue(request)

        return

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
                if (it is ShibbolethAuthenticationException) {
                    // TODO
                }

                val throwableMessage = it.message
                val message =
                    if (localPreferences.isEnabledDetailError && !throwableMessage.isNullOrEmpty()) {
                        throwableMessage
                    } else {
                        getApplication<Application>().getString(R.string.error_sync_failed)
                    }

                _indefiniteSnackbar.value = Event(message)
            }.also {
                isSyncing.value = false
            }
        }
    }
}
