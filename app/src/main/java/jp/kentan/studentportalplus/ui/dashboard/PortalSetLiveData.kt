package jp.kentan.studentportalplus.ui.dashboard

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.MyCourse
import jp.kentan.studentportalplus.data.entity.Notice

data class PortalSet(
    val myCourseList: List<MyCourse>,
    val lectureInfoList: List<LectureInformation>,
    val lectureCancelList: List<LectureCancellation>,
    val noticeList: List<Notice>
)

class PortalSetLiveData : MediatorLiveData<PortalSet>() {

    private var myCourseList: List<MyCourse>? = null
    private var lectureInfoList: List<LectureInformation>? = null
    private var lectureCancelList: List<LectureCancellation>? = null
    private var noticeList: List<Notice>? = null

    fun addMyCourseSource(liveData: LiveData<List<MyCourse>>) {
        addSource(liveData) {
            if (myCourseList == it) {
                return@addSource
            }

            myCourseList = it
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
                myCourseList = myCourseList ?: return,
                lectureInfoList = lectureInfoList ?: return,
                lectureCancelList = lectureCancelList ?: return,
                noticeList = noticeList ?: return
            )
        )
    }
}
