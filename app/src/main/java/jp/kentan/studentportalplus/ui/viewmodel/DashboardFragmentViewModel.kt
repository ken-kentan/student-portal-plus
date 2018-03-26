package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.model.Notice
import org.jetbrains.anko.coroutines.experimental.bg


class DashboardFragmentViewModel(private val portalRepository: PortalRepository) : ViewModel() {

    fun getNotices() = portalRepository.noticeLiveData

    fun getLectureInformations() = portalRepository.lectureInformationLiveData

    fun getLectureCancellations() = portalRepository.lectureCancellationLiveData

    fun updateNotice(data: Notice) = bg {
        portalRepository.update(data)
    }

    fun updateLectureInformation(data: LectureInformation) = bg {
        portalRepository.update(data)
    }

    fun updateLectureCancellation(data: LectureCancellation) = bg {
        portalRepository.update(data)
    }
}