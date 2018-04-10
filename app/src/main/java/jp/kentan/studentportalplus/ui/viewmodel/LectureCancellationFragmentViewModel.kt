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
import jp.kentan.studentportalplus.data.model.LectureCancellation
import org.jetbrains.anko.coroutines.experimental.bg


class LectureCancellationFragmentViewModel(
        private val preferences: SharedPreferences,
        private val repository: PortalRepository
) : ViewModel() {

    private val lectureCancellationList = repository.lectureCancellationList
    private val results = MediatorLiveData<List<LectureCancellation>>()
    private val _query = MutableLiveData<LectureQuery>()

    private val defaultQuery by lazy {
        val order = LectureOrderType.valueOf(
                preferences.getString("lecture_cancel_order_type", LectureOrderType.UPDATED_DATE.name)
        )
        LectureQuery.DEFAULT.copy(order = order)
    }

    var query: LectureQuery
        set(value) {
            val old = _query.value
            if (value != old) {
                _query.value = value

                if (value.order != old?.order) {
                    preferences.edit { putString("lecture_cancel_order_type", value.order.name) }
                }
            }
        }
        get() = _query.value ?: defaultQuery

    init {
        results.addSource(lectureCancellationList) {
            loadFromRepository(query)
        }

        results.addSource(_query) {
            loadFromRepository(it)
        }
    }

    fun getResults(): LiveData<List<LectureCancellation>> = results

    fun update(data: LectureCancellation) = bg {
        repository.update(data)
    }

    private fun loadFromRepository(query: LectureQuery?) {
        if (query == null || query.isDefault()) {
            results.value = lectureCancellationList.value
        } else{
            bg {
                results.postValue(repository.searchLectureCancellations(query))
            }
        }
    }
}