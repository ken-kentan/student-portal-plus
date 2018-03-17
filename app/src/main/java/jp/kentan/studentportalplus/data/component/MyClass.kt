package jp.kentan.studentportalplus.data.component


data class MyClass(
        val id          : Int = -1,
        val hash        : Int,
        val week        : ClassWeekType,
        val period      : Int,
        val scheduleCode: String,
        val credit      : Int,
        val category    : String,
        val subject     : String,
        val instructor  : String,
        val attend      : LectureAttendType,
        val location    : String? = null
)