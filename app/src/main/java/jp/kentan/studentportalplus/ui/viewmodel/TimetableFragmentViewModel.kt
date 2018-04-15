package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.ui.adapter.MyClassAdapter
import org.jetbrains.anko.coroutines.experimental.bg
import java.util.*


class TimetableFragmentViewModel(repository: PortalRepository) : ViewModel() {

    enum class LayoutType(val viewType: Int){
        WEEK(MyClassAdapter.TYPE_GRID),
        DAY(MyClassAdapter.TYPE_LIST)
    }

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

    private val source = repository.myClassList
    private val results = MediatorLiveData<List<MyClass>>()
    private val layout = MutableLiveData<LayoutType>()

    init {
        results.addSource(source) {
            layout.value?.let { loadFromRepository(it) }
        }
        results.addSource(layout) {
            it?.let { loadFromRepository(it) }
        }
    }

    fun getResults(): LiveData<List<MyClass>> = results

    fun setViewType(type: LayoutType) {
        layout.value = type
    }

    fun getWeek(): ClassWeekType? {
        val intWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        return if (intWeek in Calendar.MONDAY..Calendar.FRIDAY) {
            ClassWeekType.valueOf(intWeek-1)
        } else {
            null
        }
    }

    private fun loadFromRepository(type: LayoutType) {
        val list = source.value ?: return

        if (type == LayoutType.WEEK) {
            bg { results.postValue(normalize(list)) }
        } else {
            results.value = list
        }
    }

    /**
     * Normalize MyClass list to 7*5 timetable
     */
    private fun normalize(rawList: List<MyClass>): List<MyClass> {
        val list = mutableListOf<MyClass>()

        // id for dummy
        var uniqueId: Long = -1

        for (period in 1..7) {
            for (week in 1..5) {
                val weekType = ClassWeekType.valueOf(week)
                val myClass = rawList.find { it.match(period, weekType) }

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