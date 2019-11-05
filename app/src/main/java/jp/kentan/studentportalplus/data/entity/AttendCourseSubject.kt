package jp.kentan.studentportalplus.data.entity

import androidx.room.ColumnInfo
import jp.kentan.studentportalplus.util.JaroWinklerDistance

data class AttendCourseSubject(
    @ColumnInfo(name = "subject")
    val subject: String, // 授業科目名
    @ColumnInfo(name = "type")
    val type: AttendCourse.Type
)

fun List<AttendCourseSubject>.calcAttendCourseType(
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
