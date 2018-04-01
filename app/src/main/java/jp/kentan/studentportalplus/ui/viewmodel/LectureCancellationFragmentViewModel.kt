package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.component.isDefault
import jp.kentan.studentportalplus.data.model.LectureCancellation
import org.jetbrains.anko.coroutines.experimental.bg


class LectureCancellationFragmentViewModel(private val repository: PortalRepository) : ViewModel() {

    private val results = MediatorLiveData<List<LectureCancellation>>()
    private val _query = MutableLiveData<LectureQuery>()

    var query: LectureQuery
        set(value) {
            if (value != _query.value) {
                _query.value = value
            }
        }
        get() = _query.value ?: LectureQuery.DEFAULT

    init {
        results.addSource(repository.lectureCancellationLiveData) {
            loadFromRepository()
        }

        results.addSource(_query) {
            loadFromRepository(it)
        }
    }

    fun getResults(): LiveData<List<LectureCancellation>> = results

    fun update(data: LectureCancellation) = bg {
        repository.update(data)
    }

    private fun loadFromRepository(query: LectureQuery? = null) {
        if (query == null || query.isDefault()) {
            results.value = repository.lectureCancellationLiveData.value
        } else{
            bg {
                results.postValue(repository.searchLectureCancellations(query))
            }
        }
    }
}