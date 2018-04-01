package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.MyClass


class TimetableFragmentViewModel(private val repository: PortalRepository) : ViewModel() {

    private val results = repository.myClassLiveData

    fun getResults(): LiveData<List<MyClass>> = results
}