package jp.kentan.studentportalplus.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.work.sync.SyncWorker
import javax.inject.Inject

class MainViewModel @Inject constructor(
    application: Application,
    userRepository: UserRepository
) : AndroidViewModel(application) {

    val user = userRepository.getUser()
    val isSyncing = MutableLiveData<Boolean>()

    private val _closeDrawer = MutableLiveData<Event<Unit>>()
    val closeDrawer: LiveData<Event<Unit>>
        get() = _closeDrawer

    fun onRefresh() {
        isSyncing.value = true

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(getApplication()).run {
            getWorkInfoByIdLiveData(syncRequest.id).observeForever {
                if (it == null) {
                    return@observeForever
                }

                isSyncing.value = !it.state.isFinished
            }

            enqueue(syncRequest)
        }
    }
}
