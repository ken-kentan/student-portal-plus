package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.SharedPreferences
import androidx.core.content.edit
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureOrderType
import jp.kentan.studentportalplus.data.component.LectureQuery
import jp.kentan.studentportalplus.data.component.isDefault
import jp.kentan.studentportalplus.data.model.LectureInformation
import org.jetbrains.anko.coroutines.experimental.bg


class LectureInformationFragmentViewModel(
        private val preferences: SharedPreferences,
        private val repository: PortalRepository
) : ViewModel() {

    private val lectureInformationList = repository.lectureInformationList
    private val results = MediatorLiveData<List<LectureInformation>>()
    private val _query = MutableLiveData<LectureQuery>()

    private val defaultQuery by lazy {
        val order = LectureOrderType.valueOf(
                preferences.getString("lecture_info_order_type", LectureOrderType.UPDATED_DATE.name)
        )
        LectureQuery.DEFAULT.copy(order = order)
    }

    var query: LectureQuery
        set(value) {
            if (value != _query.value) {
                _query.value = value
                preferences.edit { putString("lecture_info_order_type", value.order.name) }
            }
        }
        get() = _query.value ?: defaultQuery

    init {
        results.addSource(lectureInformationList) {
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
            results.value = lectureInformationList.value
        } else{
            bg {
                results.postValue(repository.searchLectureInformations(query))
            }
        }
    }
}