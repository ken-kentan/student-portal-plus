package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.util.JaroWinklerDistance

abstract class LectureRepository(
    attendCourseDao: AttendCourseDao,
    localPreferences: LocalPreferences
) {

    protected val attendCourseListFlow = attendCourseDao.selectAsFlow()
    protected val similarSubjectThresholdFlow = localPreferences.similarSubjectThresholdFlow

    protected fun List<AttendCourse>.calcAttendCourseType(
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
}
