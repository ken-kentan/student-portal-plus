package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.MyClass

class MyClassViewModel(private val repository: PortalRepository) : ViewModel() {

    private lateinit var data: MyClass

    fun get(id: Long): LiveData<MyClass> =
            Transformations.map(repository.myClassList) {
                data = it.find { it.id == id } ?: return@map null
                return@map data
            }
}