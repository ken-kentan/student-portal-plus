package jp.kentan.studentportalplus.data.component

import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.data.model.Notice

data class PortalDataSet(
        val myClassList: List<MyClass> = emptyList(),
        val lectureInfoList: List<LectureInformation> = emptyList(),
        val lectureCancelList: List<LectureCancellation> = emptyList(),
        val noticeList: List<Notice> = emptyList()
)