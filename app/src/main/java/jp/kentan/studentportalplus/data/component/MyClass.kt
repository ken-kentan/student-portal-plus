package jp.kentan.studentportalplus.data.component


data class MyClass(
        val id          : Int = -1,
        val hash        : Int,           // Murmur3.hash32(week + period + scheduleCode + credit + category + subject + instructor)
        val week        : ClassWeekType, // 週
        val period      : Int,           // 時限
        val scheduleCode: String,        // 時間割コード
        val credit      : Int,           // 単位
        val category    : String,        // カテゴリ
        val subject     : String,        // 授業科目名
        val instructor  : String,        // 担当教員名
        val attend      : LectureAttendType,
        val location    : String? = null
)