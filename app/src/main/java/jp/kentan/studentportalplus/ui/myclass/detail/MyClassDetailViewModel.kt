package jp.kentan.studentportalplus.ui.myclass.detail

import androidx.lifecycle.*
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.ui.SingleLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyClassDetailViewModel(
        private val portalRepository: PortalRepository
) : ViewModel() {

    private val idLiveData = MutableLiveData<Long>()

    val myClass: LiveData<MyClass> = Transformations.switchMap(idLiveData) { id ->
        portalRepository.getMyClass(id, true)
    }

    private val myClassObserver = Observer<MyClass> { data ->
        if (data == null) {
            errorNotFound.value = Unit
        } else {
            enabledDeleteOptionMenu.value = data.isUser
        }
    }

    val startEditActivity = SingleLiveData<Long>()
    val finishActivity = SingleLiveData<Unit>()
    val enabledDeleteOptionMenu = SingleLiveData<Boolean>()
    val showDeleteDialog = SingleLiveData<String>()
    val errorDelete = SingleLiveData<Unit>()
    val errorNotFound = SingleLiveData<Unit>()

    init {
        myClass.observeForever(myClassObserver)
    }

    fun onActivityCreated(id: Long) {
        idLiveData.value = id
    }

    fun onEditClick(data: MyClass) {
        startEditActivity.value = data.id
    }

    fun onDeleteClick() {
        val data = myClass.value ?: return

        showDeleteDialog.value = data.subject
    }

    fun onDeleteConfirmClick(subject: String) {
        GlobalScope.launch {
            val isSuccess = portalRepository.deleteFromMyClass(subject).await()

            if (isSuccess) {
                finishActivity.postValue(Unit)
            } else {
                errorDelete.postValue(Unit)
            }
        }
    }

    override fun onCleared() {
        myClass.removeObserver(myClassObserver)
        super.onCleared()
    }
}