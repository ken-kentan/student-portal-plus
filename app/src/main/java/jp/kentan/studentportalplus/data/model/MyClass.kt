package jp.kentan.studentportalplus.data.model

import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.component.LectureAttendType


data class MyClass(
        val id          : Long = -1,
        val hash        : Long,          // Murmur3.hash64(week + period + scheduleCode + credit + category + subject + instructor)
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