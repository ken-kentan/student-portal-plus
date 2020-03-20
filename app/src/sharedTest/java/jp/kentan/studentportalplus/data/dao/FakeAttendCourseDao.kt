package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.AttendCourseSubject
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAttendCourseDao : AttendCourseDao {

    companion object {
        const val ALL_LIST_SIZE = 9
        const val DAY_OF_WEEK_LIST_SIZE = 5
    }

    override fun getFlow(id: Long): Flow<AttendCourse?> = flowOf(TestData.attendCourse)

    override fun getListFlow(): Flow<List<AttendCourse>> =
        flowOf(List(ALL_LIST_SIZE) { TestData.attendCourse })

    override fun getListFlow(dayOfWeek: DayOfWeek): Flow<List<AttendCourse>> =
        flowOf(List(DAY_OF_WEEK_LIST_SIZE) { TestData.attendCourse })

    override fun getSubjectListFlow(): Flow<List<AttendCourseSubject>> =
        flowOf(listOf(TestData.attendCourseSubject))

    override fun get(id: Long): AttendCourse? = TestData.attendCourse

    override fun getSubjectList(): List<AttendCourseSubject> {
        TODO("not implemented")
    }

    override fun insert(attendCourse: AttendCourse): Long {
        TODO("not implemented")
    }

    override fun insertAll(attendCourseList: List<AttendCourse>): List<Long> {
        TODO("not implemented")
    }

    override fun update(attendCourse: AttendCourse): Int {
        TODO("not implemented")
    }

    override fun delete(id: Long): Int {
        TODO("not implemented")
    }

    override fun delete(subject: String, attendType: AttendCourse.Type): Int {
        TODO("not implemented")
    }

    override fun deleteNotInHash(type: Int, hash: List<Long>) {
        TODO("not implemented")
    }
}
