package jp.kentan.studentportalplus.ui.timetable

import android.content.SharedPreferences
import androidx.lifecycle.*
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.ClassWeek
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.ui.SingleLiveData
import jp.kentan.studentportalplus.util.isGridTimetableLayout
import jp.kentan.studentportalplus.util.setGridTimetableLayout
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import java.util.*

class TimetableViewModel(
        private val preferences: SharedPreferences,
        private val portalRepository: PortalRepository
) : ViewModel() {

    companion object {
        private val EMPTY_ITEM = MyClass(
                hash = 0,
                week = ClassWeek.UNKNOWN,
                period = 1,
                scheduleCode = "",
                credit = 0,
                category = "",
                subject = "",
                instructor = "",
                isUser = false
        )
    }

    val isGridLayout = MutableLiveData<Boolean>()

    val myClassList: LiveData<List<MyClass>> = Transformations.switchMap(isGridLayout) { isGrid ->
        dayOfWeek.value = getDayOfWeek()

        if (isGrid) {
            val result = MediatorLiveData<List<MyClass>>()

            result.addSource(portalRepository.myClassList) { list ->
                GlobalScope.launch {
                    result.postValue(list.toWeekTimetable())
                }
            }

            return@switchMap result
        } else {
            return@switchMap portalRepository.myClassList
        }
    }

    val dayOfWeek = MutableLiveData<ClassWeek>()
    val notifyDataSetChanged = SingleLiveData<Unit>()
    val startDetailActivity = SingleLiveData<Long>()
    val startAddActivity = SingleLiveData<Pair<ClassWeek, Int>>()

    init {
        isGridLayout.value = preferences.isGridTimetableLayout()
    }

    fun onFragmentResume() {
        if (isGridLayout.value == true) {
            dayOfWeek.value = getDayOfWeek()
            notifyDataSetChanged.value = Unit
        }
    }

    fun onClick(id: Long) {
        startDetailActivity.value = id
    }

    fun onAddClick(week: ClassWeek, period: Int) {
        startAddActivity.value = week to period
    }

    fun onWeekLayoutClick() {
        if (isGridLayout.value != true) {
            isGridLayout.value = true
            preferences.setGridTimetableLayout(true)
        }
    }

    fun onListLayoutClick() {
        if (isGridLayout.value != false) {
            isGridLayout.value = false
            preferences.setGridTimetableLayout(false)
        }
    }

    private fun getDayOfWeek(): ClassWeek {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        return if (day in Calendar.MONDAY..Calendar.FRIDAY) {
            ClassWeek.valueOf(day-1)
        } else {
            ClassWeek.UNKNOWN
        }
    }

    private fun List<MyClass>.toWeekTimetable(): List<MyClass> {
        val list = mutableListOf<MyClass>()

        // id for empty
        var emptyId: Long = -1

        for (period in 1..7) {
            for (week in ClassWeek.TIMETABLE) {
                val myClass = find { it.period == period && it.week == week }

                if (myClass != null) {
                    list.add(myClass)
                } else {
                    list.add(EMPTY_ITEM.copy(id = emptyId--, week = week, period = period))
                }
            }
        }

        return list
    }
}
