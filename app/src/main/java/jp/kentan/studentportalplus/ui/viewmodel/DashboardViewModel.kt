package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.LectureInformation
import jp.kentan.studentportalplus.data.component.Notice
import org.jetbrains.anko.coroutines.experimental.bg


class DashboardViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    fun getNotices() = portalRepository.noticeLiveData

    fun getLectureInformations() = portalRepository.lectureInformationLiveData

    fun updateNotice(data: Notice) = bg {
        portalRepository.update(data)
    }

    fun updateLectureInformation(data: LectureInformation) = bg {
        portalRepository.update(data)
    }
}