package jp.kentan.studentportalplus.data.component


enum class PortalDataType(
        val url: String,
        val displayName: String
) {
    NOTICE("https://portal.student.kit.ac.jp", "最新情報"),
    LECTURE_INFORMATION("https://portal.student.kit.ac.jp/ead/?c=lecture_information", "授業関連連絡"),
    LECTURE_CANCELLATION("https://portal.student.kit.ac.jp/ead/?c=lecture_cancellation", "休講情報"),
    MY_CLASS("https://portal.student.kit.ac.jp/ead/?c=attend_course", "受講情報")
}