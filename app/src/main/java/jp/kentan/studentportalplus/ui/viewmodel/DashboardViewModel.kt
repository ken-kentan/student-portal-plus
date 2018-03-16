package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.Notice


class DashboardViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    val noticeLiveData = MutableLiveData<List<Notice>>()

    fun updateNoticeData(data: Notice) {
        portalRepository.updateNoticeData(data)
    }
}