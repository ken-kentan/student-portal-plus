package jp.kentan.studentportalplus.ui.myclass.edit

import android.annotation.SuppressLint
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.databinding.adapters.AdapterViewBindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.android.colorpicker.ColorPickerSwatch
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.ClassWeek
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.ui.SingleLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyClassEditViewModel(
        private val portalRepository: PortalRepository
) : ViewModel(), ColorPickerSwatch.OnColorSelectedListener {

    val title = MutableLiveData<Int>()

    val subject = ObservableField<String>()
    val color = ObservableInt()
    val instructor = ObservableField<String>()
    val location = ObservableField<String>()
    val week = ObservableInt()
    val period = ObservableInt()
    val category = ObservableField<String>()
    val credit = ObservableField<String>()
    val scheduleCode = ObservableField<String>()

    val subjects: LiveData<List<String>> = portalRepository.subjectList.map { it.sorted() }

    val weekEntries = ClassWeek.values().map { it.fullDisplayName }
    val periodEntries = (1..7).map { "${it}限" }

    val isUserMode = ObservableBoolean(true)
    val isEnabledPeriod = ObservableBoolean(true)

    val isEnabledErrorSubject = SingleLiveData<Boolean>()
    val isEnabledErrorCredit = SingleLiveData<Boolean>()
    val isEnabledErrorScheduleCode = SingleLiveData<Boolean>()

    val finishActivity = SingleLiveData<Unit>()
    val showColorPickerDialog = SingleLiveData<ColorPickerSwatch.OnColorSelectedListener>()
    val showFinishConfirmDialog = SingleLiveData<Unit>()
    val validation = SingleLiveData<ValidationResult>()
    val errorSaveFailed = SingleLiveData<Unit>()
    val errorNotFound = SingleLiveData<Unit>()

    @SuppressLint("RestrictedApi")
    val onWeekItemSelected = AdapterViewBindingAdapter.OnItemSelected { _, _, position: Int, _ ->
        val week = ClassWeek.values()[position]
        val isDisabled = week == ClassWeek.INTENSIVE || week == ClassWeek.UNKNOWN

        isEnabledPeriod.set(isUserMode.get() && !isDisabled)
    }

    private var isInitialized = false
    private var isUpdateMode = true
    private lateinit var originalData: MyClass

    init {
        subject.setErrorCancelCallback(isEnabledErrorSubject)
        credit.setErrorCancelCallback(isEnabledErrorCredit)
        scheduleCode.setErrorCancelCallback(isEnabledErrorScheduleCode)
    }

    fun onActivityCreated(id: Long) {
        isUpdateMode = true
        title.value = R.string.title_my_class_edit

        if (isInitialized) {
            return
        }
        isInitialized = true

        val data = portalRepository.getMyClassWithSync(id) ?: let {
            errorNotFound.value = Unit
            return
        }

        setData(data)
        originalData = data
    }

    fun onActivityCreated(week: ClassWeek, period: Int) {
        isUpdateMode = false
        title.value = R.string.title_my_class_add

        if (isInitialized) {
            return
        }
        isInitialized = true

        val data = MyClass(
                week = week,
                period = period,
                scheduleCode = "",
                credit = 0,
                category = "",
                subject = "",
                instructor = "",
                isUser = true
        )

        setData(data)
        originalData = data
    }

    fun onColorClick() {
        showColorPickerDialog.value = this
    }

    override fun onColorSelected(selectedColor: Int) {
        color.set(selectedColor)
    }

    fun onClickSave() {
        val subject = subject.get().trimOrEmpty()
        val credit = credit.get().trimOrEmpty().toIntOrNull() ?: 0
        val scheduleCode = scheduleCode.get().trimOrEmpty()

        // Validation
        val isErrorSubject = subject.isBlank()
        val isErrorCredit = credit !in 0..10
        val isErrorScheduleCode = scheduleCode.isNotScheduleCode()

        if (isErrorSubject || isErrorCredit || isErrorScheduleCode) {
            validation.value = ValidationResult(isErrorSubject, isErrorCredit, isErrorScheduleCode)
            return
        }


        val instructor = instructor.get().trimOrEmpty()
        val location = location.get().trimOrNull()
        val weekType = ClassWeek.values()[week.get()]
        val period = if (weekType.hasPeriod()) period.get() + 1 else 0
        val category = category.get().trimOrEmpty()

        val data = MyClass(
                id = originalData.id,
                isUser = isUserMode.get(),
                subject = subject,
                instructor = instructor,
                location = location,
                week = weekType,
                period = period,
                category = category,
                credit = credit,
                scheduleCode = scheduleCode,
                color = color.get()
        )

        GlobalScope.launch {

            val isSuccess = if (isUpdateMode) {
                portalRepository.updateMyClass(data).await()
            } else {
                portalRepository.addMyClass(data).await()
            }

            if (isSuccess) {
                finishActivity.postValue(Unit)
            } else {
                errorSaveFailed.postValue(Unit)
            }
        }
    }

    fun onFinish() {
        val data = originalData
        val dataLocation = data.run { location ?: "" }
        val dataCredit: String? = data.run { if (credit > 0) credit.toString() else "" }

        val period = period.get() + 1

        val canFinish = (color.get() == data.color) &&
                (subject.get() == data.subject) &&
                (instructor.get() == data.instructor) &&
                (location.get() == dataLocation) &&
                (week.get() == data.week.ordinal) &&
                ((period == data.period || !data.week.hasPeriod())) &&
                (category.get() == data.category) &&
                (credit.get() == dataCredit) &&
                (scheduleCode.get() == data.scheduleCode)

        if (canFinish) {
            finishActivity.value = Unit
        } else {
            showFinishConfirmDialog.value = Unit
        }
    }

    private fun setData(data: MyClass) {
        isUserMode.set(data.isUser)

        subject.set(data.subject)
        color.set(data.color)
        instructor.set(data.instructor)
        location.set(data.location ?: "")
        week.set(data.week.ordinal)
        period.set(if (data.period in 1..7) data.period - 1 else 0)
        category.set(data.category)
        credit.set(if (data.credit > 0) data.credit.toString() else "")
        scheduleCode.set(data.scheduleCode)
    }

    private fun ObservableField<String>.setErrorCancelCallback(error: SingleLiveData<Boolean>) {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                error.value = false
            }
        })
    }

    private fun String?.trimOrEmpty(): String = this?.trim() ?: ""

    private fun String?.trimOrNull(): String? {
        val trim = this?.trim()

        return if (trim.isNullOrEmpty()) null else trim
    }

    private fun String.isNotScheduleCode(): Boolean {
        val code = toIntOrNull() ?: return !isEmpty()
        return code !in 10000000..1000000000
    }
}