package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.databinding.adapters.AdapterViewBindingAdapter
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.util.Murmur3
import jp.kentan.studentportalplus.util.trimOrEmpty
import org.jetbrains.anko.coroutines.experimental.bg

class MyClassEditViewModel(
        private val repository: PortalRepository
) : ViewModel() {

    enum class Mode(val title: String) {
        EDIT("Edit"),
        ADD("Add")
    }

    val subjects: LiveData<List<String>> = Transformations.map(repository.subjectList) { it.sorted() }

    val weekEntries = ClassWeekType.values().map { it.fullDisplayName }
    val periodEntries = (1..7).map { "${it}限" }

    val isUser = ObservableBoolean()
    val enabledPeriod = ObservableBoolean()

    val color = ObservableInt()

    val subject = ObservableField<String>()
    val instructor = ObservableField<String>()
    val location = ObservableField<String>()
    val week = ObservableInt()
    val period = ObservableInt()
    val category = ObservableField<String>()
    val credit = ObservableField<String>()
    val scheduleCode = ObservableField<String>()

    val onWeekItemSelected = AdapterViewBindingAdapter.OnItemSelected { _, _, position: Int, _ ->
        val week = ClassWeekType.values().getOrNull(position) ?: ClassWeekType.UNKNOWN
        val enabled = (week != ClassWeekType.INTENSIVE) && (week != ClassWeekType.UNKNOWN)

        enabledPeriod.set(isUser.get() && enabled)
    }

    private val weekType: ClassWeekType
        get() = ClassWeekType.values().getOrNull(week.get()) ?: ClassWeekType.UNKNOWN

    private lateinit var originalData: MyClass

    @Throws(Exception::class)
    fun startEdit(id: Long) {
        if (::originalData.isInitialized) {
            return
        }

        val data = repository.getMyClassById(id) ?:
                throw IllegalStateException("data_ not found")

        originalData = data
        setData(data)
    }

    fun startAdd(week: ClassWeekType, period: Int) {
        if (::originalData.isInitialized) {
            return
        }

        val data = MyClass(
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

        originalData = data
        setData(data)
    }

    private fun setData(data: MyClass) {
        isUser.set(data.isUser)
        enabledPeriod.set(data.isUser)
        color.set(data.color)
        subject.set(data.subject)
        instructor.set(data.instructor)
        location.set(data.location)
        week.set(data.week.ordinal)
        period.set(if (data.period in 1..7) data.period - 1 else 0)
        category.set(data.category)
        credit.set(data.credit.toString())
        scheduleCode.set(data.scheduleCode)
    }

    fun save() = bg {
        val subject = subject.get().trimOrEmpty()
        val instructor = instructor.get().trimOrEmpty()
        val location = location.get().trimOrEmpty()
        val week = weekType
        val period = if (week.hasPeriod()) period.get() + 1 else 0
        val category = category.get().trimOrEmpty()
        val creditStr = credit.get().trimOrEmpty()
        val scheduleCode = scheduleCode.get().trimOrEmpty()

        val credit = creditStr.toIntOrNull() ?: 0
        val hashStr = week.name + period + scheduleCode + credit + category + subject + instructor + isUser.get()

        val data = originalData.copy(
                hash = Murmur3.hash64(hashStr.toByteArray()),
                subject = subject,
                instructor = instructor,
                location = location,
                week = week,
                period = period,
                category = category,
                credit = creditStr.toIntOrNull() ?: 0,
                scheduleCode = scheduleCode,
                color = color.get()
        )

        return@bg if (data.id > 0) repository.update(data) else repository.add(data)
    }


    fun hasEdit(): Boolean {
        val data = originalData
        val period = period.get() + 1

        return (color.get() != data.color) ||
                (subject.get() != data.subject) ||
                (instructor.get() != data.instructor) ||
                (location.get() != data.location) ||
                (weekType != data.week) ||
                (data.week.hasPeriod() && (period != data.period)) ||
                (category.get() != data.category) ||
                (credit.get() != data.credit.toString()) ||
                (scheduleCode.get() != data.scheduleCode)
    }
}