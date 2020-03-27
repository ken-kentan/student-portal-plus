package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.flow.Flow

class FakeAttendCourseDao : AttendCourseDao {

    override fun selectAsFlow(id: Long): Flow<AttendCourse?> {
        TODO("Not yet implemented")
    }

    override fun selectAsFlow(): Flow<List<AttendCourse>> {
        TODO("Not yet implemented")
    }

    override fun selectAsFlow(dayOfWeek: DayOfWeek): Flow<List<AttendCourse>> {
        TODO("Not yet implemented")
    }

    override fun select(id: Long): AttendCourse? {
        TODO("Not yet implemented")
    }

    override fun select(): List<AttendCourse> {
        TODO("Not yet implemented")
    }

    override fun insert(attendCourse: AttendCourse): Long {
        TODO("Not yet implemented")
    }

    override fun insert(attendCourseList: List<AttendCourse>): List<Long> {
        TODO("Not yet implemented")
    }

    override fun update(attendCourse: AttendCourse): Int {
        TODO("Not yet implemented")
    }

    override fun delete(id: Long): Int {
        TODO("Not yet implemented")
    }

    override fun delete(subject: String, attendType: AttendCourse.Type): Int {
        TODO("Not yet implemented")
    }

    override fun deleteNotInHash(attendType: AttendCourse.Type, hash: List<Long>) {
        TODO("Not yet implemented")
    }
}
