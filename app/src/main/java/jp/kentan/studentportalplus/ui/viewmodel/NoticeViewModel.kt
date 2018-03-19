package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.Notice
import org.jetbrains.anko.coroutines.experimental.bg

class NoticeViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    fun getNotice(id: Long): MutableLiveData<Notice?> {
        val liveData = MutableLiveData<Notice?>()

        bg {
            val data = portalRepository.getNoticeById(id)
            liveData.postValue(data)
        }

        return liveData
    }
}