package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider
import org.jetbrains.anko.coroutines.experimental.bg

class MainViewModel(
        private val repository: PortalRepository,
        private val shibbolethDataProvider: ShibbolethDataProvider
) : ViewModel() {

    private val _isSyncing = MutableLiveData<Boolean>()
    private val syncResult = MutableLiveData<Pair<Boolean, String?>>()

    fun load() {
        bg { repository.loadFromDb() }
    }

    fun sync() {
        _isSyncing.value = true

        bg {
            syncResult.postValue(repository.syncWithWeb())
            _isSyncing.postValue(false)
        }
    }

    fun getSyncResult(): LiveData<Pair<Boolean, String?>> {
        val result = MediatorLiveData<Pair<Boolean, String?>>()
        result.addSource(syncResult) {
            result.value = it
        }
        return result
    }

    fun isSyncing(): LiveData<Boolean> {
        val result = MediatorLiveData<Boolean>()
        result.addSource(_isSyncing) {
            result.value = it
        }
        return result
    }

    fun getUser() = shibbolethDataProvider.getUser()
}