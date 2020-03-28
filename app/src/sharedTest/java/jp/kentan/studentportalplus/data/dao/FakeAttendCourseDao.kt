package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.flow.flowOf

class FakeAttendCourseDao : AttendCourseDao {

    override fun selectAsFlow(id: Long) = flowOf(
        if (id == TestData.attendCourse.id) TestData.attendCourse else null
    )

    override fun selectAsFlow() = flowOf(
        List(3) { TestData.attendCourse }
    )

    override fun selectAsFlow(dayOfWeek: DayOfWeek) = flowOf(
        if (dayOfWeek == TestData.attendCourse.dayOfWeek) List(3) { TestData.attendCourse } else emptyList()
    )

    override fun select(id: Long) =
        if (id == TestData.attendCourse.id) TestData.attendCourse else null

    override fun select() = List(3) { TestData.attendCourse }

    override fun insert(attendCourse: AttendCourse) = 1L

    override fun insert(attendCourseList: List<AttendCourse>) =
        List(attendCourseList.size) { 1L }

    override fun insertIgnore(attendCourseList: List<AttendCourse>) =
        List(attendCourseList.size) { 1L }

    override fun update(attendCourse: AttendCourse) = 1

    override fun delete(id: Long) = if (id == TestData.attendCourse.id) 1 else 0

    override fun delete(subject: String, attendType: AttendCourse.Type) =
        if (subject == TestData.attendCourse.subject && attendType == TestData.attendCourse.type) 1 else 0

    override fun deleteNotInHash(attendType: AttendCourse.Type, hash: List<Long>) {}
}
