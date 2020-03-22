package jp.kentan.studentportalplus.ui.dashboard

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice

data class PortalSet(
    val attendCourseList: List<AttendCourse>,
    val lectureInfoList: List<LectureInformation>,
    val lectureCancelList: List<LectureCancellation>,
    val noticeList: List<Notice>
)

class PortalSetLiveData : MediatorLiveData<PortalSet>() {

    private var attendCourseList: List<AttendCourse>? = null
    private var lectureInfoList: List<LectureInformation>? = null
    private var lectureCancelList: List<LectureCancellation>? = null
    private var noticeList: List<Notice>? = null

    fun addAttendCourseSource(liveData: LiveData<List<AttendCourse>>) {
        addSource(liveData) {
            if (attendCourseList == it) {
                return@addSource
            }

            attendCourseList = it
            setValueIfReady()
        }
    }

    fun addLectureInformationSource(liveData: LiveData<List<LectureInformation>>) {
        addSource(liveData) {
            if (lectureInfoList == it) {
                return@addSource
            }

            lectureInfoList = it
            setValueIfReady()
        }
    }

    fun addLectureCancellationSource(liveData: LiveData<List<LectureCancellation>>) {
        addSource(liveData) {
            if (lectureCancelList == it) {
                return@addSource
            }

            lectureCancelList = it
            setValueIfReady()
        }
    }

    fun addNoticeSource(liveData: LiveData<List<Notice>>) {
        addSource(liveData) {
            if (noticeList == it) {
                return@addSource
            }

            noticeList = it
            setValueIfReady()
        }
    }

    override fun setValue(value: PortalSet?) = throw UnsupportedOperationException()

    override fun postValue(value: PortalSet?) = throw UnsupportedOperationException()

    @MainThread
    private fun setValueIfReady() {
        super.setValue(
            PortalSet(
                attendCourseList = attendCourseList ?: return,
                lectureInfoList = lectureInfoList ?: return,
                lectureCancelList = lectureCancelList ?: return,
                noticeList = noticeList ?: return
            )
        )
    }
}
