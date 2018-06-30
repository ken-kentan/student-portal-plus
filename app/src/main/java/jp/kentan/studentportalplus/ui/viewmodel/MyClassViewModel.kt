package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.MyClass
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg

class MyClassViewModel(private val repository: PortalRepository) : ViewModel() {

    private val myClassSource = repository.myClassList

    val myClassId = MutableLiveData<Long>()
    val myClass: LiveData<MyClass> by lazy {
        val result = MediatorLiveData<MyClass>()
        result.addSource(myClassId) { id ->
            result.value = findMyClassById(id)
        }
        result.addSource(myClassSource) {
            val data = findMyClassById(myClassId.value) ?: return@addSource
            result.value = data
        }

        return@lazy result
    }

    fun onClickDelete(onDeleted: (isSuccess: Boolean) -> Unit) {
        val data = myClass.value ?: return

        launch(UI) {
            val success = bg { repository.delete(data.subject) }.await()
            onDeleted(success)
        }
    }

    private fun findMyClassById(id: Long?) = myClassSource.value?.find { it.id == id }
}