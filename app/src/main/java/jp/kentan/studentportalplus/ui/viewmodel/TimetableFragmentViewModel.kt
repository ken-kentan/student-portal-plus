package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.model.MyClass
import org.jetbrains.anko.coroutines.experimental.bg


class TimetableFragmentViewModel(repository: PortalRepository) : ViewModel() {

    private companion object {
        val DUMMY = MyClass(
                id = -1,
                hash = -1,
                week = ClassWeekType.UNKNOWN,
                period = 1,
                scheduleCode = "",
                credit = 0,
                category = "",
                subject = "",
                instructor = "",
                isUser = false,
                location = null)
    }

    private val results = MediatorLiveData<List<MyClass>>()

    init {
        results.addSource(repository.myClassLiveData) {
            bg {
                results.postValue(normalize(it))
            }
        }
    }

    fun getResults(): LiveData<List<MyClass>> = results

    /**
     * Normalize MyClass list to 7*5 timetable
     */
    private fun normalize(rawList: List<MyClass>?): List<MyClass> {
        val list = mutableListOf<MyClass>()

        // id for dummy
        var uniqueId: Long = -1

        for (period in 1..7) {
            for (week in 1..5) {
                val weekType = ClassWeekType.valueOf(week)
                val myClass = rawList?.find { it.match(period, weekType) }

                if (myClass != null) {
                    list.add(myClass)
                } else {
                    list.add(DUMMY.copy(id = uniqueId--, week = weekType, period = period))
                }
            }
        }

        return list
    }
}