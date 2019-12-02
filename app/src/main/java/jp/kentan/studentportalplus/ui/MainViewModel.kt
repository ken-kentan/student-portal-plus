package jp.kentan.studentportalplus.ui

import android.app.Application
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.work.sync.SyncWorker
import javax.inject.Inject

class MainViewModel @Inject constructor(
    application: Application,
    userRepository: UserRepository,
    private val localPreferences: LocalPreferences
) : AndroidViewModel(application), Observer<WorkInfo> {

    val user = userRepository.getFlow().asLiveData()
    val isSyncing = MutableLiveData<Boolean>()

    private val _indefiniteSnackbar = MutableLiveData<Event<String>>()
    val indefiniteSnackbar: LiveData<Event<String>>
        get() = _indefiniteSnackbar

    private val _closeDrawer = MutableLiveData<Event<Unit>>()
    val closeDrawer: LiveData<Event<Unit>>
        get() = _closeDrawer

    private val workManager = WorkManager.getInstance(application)

    private var workInfoLiveData: LiveData<WorkInfo>? = null
        set(value) {
            value?.observeForever(this)
            field = value
        }

    fun onRefresh() {
        if (isSyncing.value == true) {
            return
        }

        isSyncing.value = true

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()

        workInfoLiveData = workManager.getWorkInfoByIdLiveData(syncRequest.id)

        workManager.enqueue(syncRequest)
    }

    override fun onChanged(workInfo: WorkInfo?) {
        if (workInfo == null || !workInfo.state.isFinished) {
            return
        }

        isSyncing.value = false
        workInfoLiveData?.removeObserver(this)

        var message: String? = null

        when (workInfo.state) {
            WorkInfo.State.FAILED -> {
                if (localPreferences.isEnabledDetailError) {
                    message = workInfo.outputData.getString(SyncWorker.KEY_DATA_MESSAGE)
                }

                if (message == null) {
                    message = getApplication<Application>().getString(R.string.error_sync_failed)
                }
            }
            WorkInfo.State.CANCELLED -> {
                message = getApplication<Application>().getString(R.string.error_sync_cancelled)
            }
            else -> return
        }

        _indefiniteSnackbar.value = Event(message)
    }
}
