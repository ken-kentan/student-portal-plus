package jp.kentan.studentportalplus

import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.MyCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.MyCourseType
import java.util.Date

object TestData {
    val attendCourse = MyCourse(
        id = 1,
        dayOfWeek = DayOfWeek.MONDAY,
        period = 1,
        scheduleCode = "scheduleCode",
        credit = 2,
        category = "category",
        subject = "subject",
        instructor = "instructor",
        isEditable = true
    )

    val lectureInfo = LectureInformation(
        id = 234,
        grade = "grade",
        semester = "semester",
        subject = "subject",
        instructor = "instructor",
        dayOfWeek = "dayOfWeek",
        period = "period",
        category = "category",
        detailText = "detailText",
        detailHtml = "detailHtml",
        createdDate = Date(0),
        updatedDate = Date(0),
        myCourseType = MyCourseType.EDITABLE,
        isRead = false
    )
}
