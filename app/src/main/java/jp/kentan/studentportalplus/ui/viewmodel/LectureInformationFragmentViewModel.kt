package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureOrderType
import jp.kentan.studentportalplus.data.model.LectureInformation
import org.jetbrains.anko.coroutines.experimental.bg


class LectureInformationFragmentViewModel(private val repository: PortalRepository) : ViewModel() {

    private companion object {
        val DEFAULT_FILTER = Filter(LectureOrderType.UPDATED_DATE, true, true, true)
    }

    private val results = MediatorLiveData<List<LectureInformation>>()
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
        results.addSource(repository.lectureInformationLiveData) {
            loadFromRepository()
        }

        results.addSource(_query) {
            loadFromRepository()
        }

        results.addSource(_filter) {
            loadFromRepository()
        }
    }

    fun getResults(): LiveData<List<LectureInformation>> = results

    fun update(data: LectureInformation) = bg {
        repository.update(data)
    }

    private fun loadFromRepository() {
        if (_query.value.isNullOrBlank() && _filter.value.isNullOrDefault()) {
            results.value = repository.lectureInformationLiveData.value
        } else{
            bg {
                results.postValue(repository.searchLectureInformations(query, filter.type, filter.isUnread, filter.isRead, filter.isAttend))
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
            val type: LectureOrderType,
            val isUnread: Boolean,
            val isRead: Boolean,
            val isAttend: Boolean)
}