package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.component.isDefault
import jp.kentan.studentportalplus.data.model.LectureInformation
import org.jetbrains.anko.coroutines.experimental.bg


class LectureInformationFragmentViewModel(private val repository: PortalRepository) : ViewModel() {

    private val results = MediatorLiveData<List<LectureInformation>>()
    private val _query = MutableLiveData<LectureQuery>()

    var query: LectureQuery
        set(value) {
            if (value != _query.value) {
                _query.value = value
            }
        }
        get() = _query.value ?: LectureQuery.DEFAULT

    init {
        results.addSource(repository.lectureInformationLiveData) {
            loadFromRepository(query)
        }

        results.addSource(_query) {
            loadFromRepository(it)
        }
    }

    fun getResults(): LiveData<List<LectureInformation>> = results

    fun update(data: LectureInformation) = bg {
        repository.update(data)
    }

    private fun loadFromRepository(query: LectureQuery?) {
        if (query == null || query.isDefault()) {
            results.value = repository.lectureInformationLiveData.value
        } else{
            bg {
                results.postValue(repository.searchLectureInformations(query))
            }
        }
    }
}