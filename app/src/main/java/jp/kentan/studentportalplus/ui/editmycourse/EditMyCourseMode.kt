package jp.kentan.studentportalplus.ui.editmycourse

import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period

sealed class EditMyCourseMode {

    class Update(
        val id: Long
    ) : EditMyCourseMode()

    class Add(
        val period: Period,
        val dayOfWeek: DayOfWeek
    ) : EditMyCourseMode()
}
