package jp.kentan.studentportalplus.data.component


enum class PortalDataType(val url: String) {
    NOTICE("https://portal.student.kit.ac.jp"),
    LECTURE_INFORMATION("https://portal.student.kit.ac.jp/ead/?c=lecture_information"),
    LECTURE_CANCELLATION("https://portal.student.kit.ac.jp/ead/?c=lecture_cancellation"),
    MY_CLASS("https://portal.student.kit.ac.jp/ead/?c=attend_course")
}