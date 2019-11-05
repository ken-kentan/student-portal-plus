package jp.kentan.studentportalplus

import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.AttendCourseSubject
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import java.util.*

object TestData {
    val attendCourse = AttendCourse(
        id = 123,
        dayOfWeek = DayOfWeek.MONDAY,
        period = 1,
        scheduleCode = "scheduleCode",
        credit = 2,
        category = "category",
        subject = "subject",
        instructor = "instructor",
        type = AttendCourse.Type.USER
    )

    val attendCourseSubject = AttendCourseSubject(
        subject = "subject",
        type = AttendCourse.Type.PORTAL
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
        createdDate = Date(),
        updatedDate = Date(),
        isRead = false
    )
}