package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.model.Notice
import org.jetbrains.anko.coroutines.experimental.bg


class DashboardFragmentViewModel(private val repository: PortalRepository) : ViewModel() {

    private companion object {
        const val MAX_LIST_SIZE = 3
    }

    fun getNoticeList(): LiveData<List<Notice>> = repository.noticeList

    fun getAttendLectureInformationList(): LiveData<List<LectureInformation>> =
            Transformations.map(repository.lectureInformationList) { it.filter { it.attend.isAttend() } }

    fun getAttendLectureCancellationList(): LiveData<List<LectureCancellation>> =
            Transformations.map(repository.lectureCancellationList) { it.filter { it.attend.isAttend() } }

    fun updateNotice(data: Notice) = bg {
        repository.update(data)
    }

    fun updateLectureInformation(data: LectureInformation) = bg {
        repository.update(data)
    }

    fun updateLectureCancellation(data: LectureCancellation) = bg {
        repository.update(data)
    }
}