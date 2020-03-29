package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.entity.MyCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.flow.flowOf

class FakeMyCourseDao : MyCourseDao {

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

    override fun insert(myCourse: MyCourse) = 1L

    override fun insert(myCourseList: List<MyCourse>) =
        List(myCourseList.size) { 1L }

    override fun insertIgnore(myCourseList: List<MyCourse>) = Unit

    override fun update(myCourse: MyCourse) = 1

    override fun delete(id: Long) = if (id == TestData.attendCourse.id) 1 else 0

    override fun delete(subject: String) =
        if (subject == TestData.attendCourse.subject && TestData.attendCourse.isEditable) 1 else 0

    override fun deleteNotInHash(hash: List<Long>) {}
}
