package jp.kentan.studentportalplus.data.component

enum class LectureOrderType(private val string: String) {
    UPDATED_DATE("最終更新日"),
    ATTEND_CLASS("受講科目優先");

    override fun toString() = string
}