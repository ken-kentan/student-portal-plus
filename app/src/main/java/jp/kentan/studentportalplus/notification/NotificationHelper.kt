package jp.kentan.studentportalplus.notification

import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice

abstract class NotificationHelper {

    companion object {
        val VIBRATION_PATTERN = longArrayOf(0, 300, 300, 300)
    }

    abstract fun sendLectureInformation(lectureInfoList: List<LectureInformation>)
    abstract fun sendLectureCancellation(lectureCancelList: List<LectureCancellation>)
    abstract fun sendNotice(noticeList: List<Notice>)
}
