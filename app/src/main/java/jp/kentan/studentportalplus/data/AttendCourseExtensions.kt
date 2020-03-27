package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.util.JaroWinklerDistance

fun List<AttendCourse>.resolveAttendCourseType(
    subject: String,
    threshold: Float
): AttendCourse.Type {
    firstOrNull { it.subject == subject }?.let {
        return it.type
    }

    if (any { JaroWinklerDistance.getDistance(it.subject, subject) >= threshold }) {
        return AttendCourse.Type.SIMILAR
    }

    return AttendCourse.Type.NOT
}
