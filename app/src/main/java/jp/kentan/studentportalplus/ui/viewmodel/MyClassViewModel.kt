package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.MyClass
import org.jetbrains.anko.coroutines.experimental.bg

class MyClassViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    private lateinit var data: MyClass

    private var id: Long = -1
    private val result = MediatorLiveData<MyClass>()

    init {
        result.addSource(portalRepository.myClassLiveData) {
            bg {
                if (id > 0) {
                    result.postValue(portalRepository.getMyClassById(id))
                }
            }
        }
    }

    fun get(id: Long): LiveData<MyClass> {
        this.id = id
        bg { result.postValue(portalRepository.getMyClassById(id)) }

        return result
    }
}