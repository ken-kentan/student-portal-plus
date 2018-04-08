package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.util.Murmur3
import org.jetbrains.anko.coroutines.experimental.bg

class MyClassEditViewModel(private val repository: PortalRepository) : ViewModel() {

    val subjects: LiveData<List<String>> = Transformations.map(repository.subjectList) { it.sorted() }

    private lateinit var data: MyClass
    private var editData: MyClass? = null

    fun get(id: Long): MyClass? {
        editData?.let { return it }

        data = repository.getMyClassById(id) ?: return null
        return data
    }

    fun create(week: ClassWeekType, period: Int): MyClass {
        editData?.let { return it }

        data = MyClass(
                hash = 0,
                week = week,
                period = period,
                scheduleCode = "",
                credit = 2,
                category = "",
                subject = "",
                instructor = "",
                isUser = true
        )
        return data
    }

    fun getEditData() = editData ?: data

    fun save() = bg {
        editData?.let {
            val period = if (it.week.hasPeriod()) it.period else 0
            val hashStr = it.week.name + period + it.scheduleCode + it.credit + it.category + it.subject + it.instructor
            val data = it.copy(hash = Murmur3.hash64(hashStr.toByteArray()), period = period)

            return@bg if (data.id > 0) repository.update(data) else repository.add(data)
        }
        return@bg false
    }

    /**
     * Store edit data (not update)
     */
    fun edit(data: MyClass) {
        editData = data
    }

    fun hasEdit(): Boolean {
        val edit = editData ?: return false

        if (edit.color != data.color) {
            return true
        }

        return  (edit.week != data.week) ||
                (data.week.hasPeriod() && (edit.period != data.period)) ||
                (edit.scheduleCode != data.scheduleCode) ||
                (edit.credit != data.credit) ||
                (edit.category != data.category) ||
                (edit.subject != data.subject) ||
                (edit.instructor != data.instructor) ||
                (edit.color != data.color) ||
                (edit.location != data.location)
    }
}