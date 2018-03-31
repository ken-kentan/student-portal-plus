package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.NoticeQuery
import jp.kentan.studentportalplus.data.component.isDefault
import jp.kentan.studentportalplus.data.model.Notice
import org.jetbrains.anko.coroutines.experimental.bg


class NoticeFragmentViewModel(private val repository: PortalRepository) : ViewModel() {

    private val results = MediatorLiveData<List<Notice>>()
    private val _query  = MutableLiveData<NoticeQuery>()

    var query: NoticeQuery
        set(value) {
            if (value != _query.value) {
                _query.value = value
            }
        }
        get() = _query.value ?: NoticeQuery.DEFAULT

    init {
        results.addSource(repository.noticeLiveData) {
            loadFromRepository()
        }

        results.addSource(_query) {
            loadFromRepository(it)
        }
    }

    fun getResults(): LiveData<List<Notice>> = results

    fun update(data: Notice) = bg {
        repository.update(data)
    }

    private fun loadFromRepository(query: NoticeQuery? = null) {
        if (query == null || query.isDefault()) {
            results.value = repository.noticeLiveData.value
        } else{
            bg {
                results.postValue(repository.searchNotices(query))
            }
        }
    }
}