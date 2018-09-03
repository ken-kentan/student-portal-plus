package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.MyClass
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg

class MyClassViewModel(
        private val repository: PortalRepository
) : ViewModel() {

    private val myClassSource = repository.myClassList

    private val myClassId = MutableLiveData<Long>()
    val myClass: LiveData<MyClass> by lazy(LazyThreadSafetyMode.NONE) {
        MediatorLiveData<MyClass>().apply {
            addSource(myClassId) { id ->
                value = myClassSource.value?.find { it.id == id }
            }
            addSource(myClassSource) { list ->
                val id = myClassId.value ?: return@addSource
                value = list?.find { it.id == id }
            }
        }
    }

    val subject: String
        get() = myClass.value?.subject ?: ""

    val canDelete: Boolean
        get() = myClass.value?.isUser == true

    fun setId(id: Long) {
        myClassId.value = id
    }

    fun delete(onDeleted: (isSuccess: Boolean) -> Unit) {
        val data = myClass.value ?: return

        launch(UI) {
            val success = bg { repository.delete(data.subject) }.await()
            onDeleted(success)
        }
    }
}