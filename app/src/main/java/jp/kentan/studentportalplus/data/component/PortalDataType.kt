package jp.kentan.studentportalplus.data.component


enum class PortalDataType(
        val url: String,
        val displayName: String,
        val notifyTypeKey: String
) {
    NOTICE("https://portal.student.kit.ac.jp", "最新情報", "notify_type_notice"),
    LECTURE_INFORMATION("https://portal.student.kit.ac.jp/ead/?c=lecture_information", "授業関連連絡", "notify_type_lecture_info"),
    LECTURE_CANCELLATION("https://portal.student.kit.ac.jp/ead/?c=lecture_cancellation", "休講情報", "notify_type_lecture_cancel"),
    MY_CLASS("https://portal.student.kit.ac.jp/ead/?c=attend_course", "受講情報", "notify_type_my_class")
}