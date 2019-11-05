package jp.kentan.studentportalplus.ui.editattendcourse

import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period

sealed class EditAttendCourseMode {
    class Update(
        val id: Long
    ) : EditAttendCourseMode()

    class Add(
        val period: Period,
        val dayOfWeek: DayOfWeek
    ) : EditAttendCourseMode()
}
