package jp.kentan.studentportalplus.data.component


enum class LectureAttendType {
    PORTAL,  // ポータル取得
    USER,    // ユーザー登録
    SIMILAR, // 類似
    NOT,     // 未受講
    UNKNOWN; // 未確認

    fun isAttend() = this == PORTAL || this == USER || this == SIMILAR

    fun canAttend() = this == SIMILAR || this == NOT
}