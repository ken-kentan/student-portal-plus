package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.*
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.Notice
import org.jetbrains.anko.coroutines.experimental.bg


class NoticeFragmentViewModel(private val repository: PortalRepository) : ViewModel() {

    private val results = MediatorLiveData<List<Notice>>()
    private val _query  = MutableLiveData<String>()

    var query: String?
        set(value) {
            if (value != _query.value) {
                _query.value = value
            }
        }
        get() = _query.value

    init {
        results.addSource(repository.noticeLiveData) {
            _query.value = _query.value
        }

        results.addSource(_query) {
            if (it == null || it.isBlank()) {
                results.value = repository.noticeLiveData.value
            } else{
                bg { results.postValue(repository.searchNotices(it)) }
            }
        }
    }

    fun getNotices(): LiveData<List<Notice>> = results

    fun updateNotice(data: Notice) = bg {
        repository.update(data)
    }
}