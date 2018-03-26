package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.*
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.data.component.CreatedDateType
import org.jetbrains.anko.coroutines.experimental.bg


class NoticeFragmentViewModel(private val repository: PortalRepository) : ViewModel() {

    private companion object {
        val DEFAULT_FILTER = Filter(CreatedDateType.ALL, true, true, true)
    }

    private val results = MediatorLiveData<List<Notice>>()
    private val _query  = MutableLiveData<String>()
    private val _filter = MutableLiveData<Filter>()

    var query: String?
        set(value) {
            if (value != _query.value) {
                _query.value = value
            }
        }
        get() = _query.value

    var filter: Filter
        set(value) {
            if (value != _filter.value) {
                _filter.value = value
            }
        }
        get() = _filter.value ?: DEFAULT_FILTER

    init {
        results.addSource(repository.noticeLiveData) {
            loadFromRepository()
        }

        results.addSource(_query) {
            loadFromRepository()
        }

        results.addSource(_filter) {
            loadFromRepository()
        }
    }

    fun getNotices(): LiveData<List<Notice>> = results

    fun updateNotice(data: Notice) = bg {
        repository.update(data)
    }

    private fun loadFromRepository() {
        if (_query.value.isNullOrBlank() && _filter.value.isNullOrDefault()) {
            results.value = repository.noticeLiveData.value
        } else{
            bg {
                results.postValue(repository.searchNotices(query, filter.type, filter.isUnread, filter.isRead, filter.isFavorite))
            }
        }
    }

    private fun Filter?.isNullOrDefault(): Boolean {
        if (this == null) {
            return true
        }

        return this == DEFAULT_FILTER
    }

    data class Filter(
            val type: CreatedDateType,
            val isUnread: Boolean,
            val isRead: Boolean,
            val isFavorite: Boolean)
}