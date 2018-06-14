package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider
import org.jetbrains.anko.coroutines.experimental.bg

class MainViewModel(
        private val repository: PortalRepository,
        private val shibbolethDataProvider: ShibbolethDataProvider
) : ViewModel() {

    private val _isSyncing = MutableLiveData<Boolean>()
    private val _syncResult = MutableLiveData<Pair<SyncResult, String?>>()

    val isSyncing: LiveData<Boolean> = Transformations.map(_isSyncing) { it }
    val syncResult: LiveData<Pair<SyncResult, String?>> = Transformations.map(_syncResult) { it }

    enum class SyncResult { SUCCESS, AUTH_ERROR, UNKNOWN_ERROR }

    fun load() {
        bg { repository.loadFromDb() }
    }

    fun sync() {
        _isSyncing.value = true

        bg {
            try {
                repository.sync()
                _syncResult.postValue(Pair(SyncResult.SUCCESS, null))
            } catch (e: ShibbolethAuthenticationException) {
                _syncResult.postValue(Pair(SyncResult.AUTH_ERROR, e.message))
            } catch (e: Exception) {
                _syncResult.postValue(Pair(SyncResult.UNKNOWN_ERROR, e.message))
            } finally {
                _isSyncing.postValue(false)
            }
        }
    }

    fun getUser() = shibbolethDataProvider.getUser()
}