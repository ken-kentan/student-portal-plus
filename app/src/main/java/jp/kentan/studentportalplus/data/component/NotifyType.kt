package jp.kentan.studentportalplus.data.component

enum class NotifyType(
        val displayName: String
) {
    ALL("すべて"),
    ATTEND("受講科目・類似科目"),
    NOT("通知しない")
}