package jp.kentan.studentportalplus.data

import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.FakeAttendCourseDao
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.Lecture
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultAttendCourseRepositoryTest {

    private val attendCourseDao = spyk<AttendCourseDao>(FakeAttendCourseDao())

    private val repository = DefaultAttendCourseRepository(attendCourseDao)

    @Test
    fun `getAsFlow should return AttendCourse if exist`() = runBlocking {
        val flow = repository.getAsFlow(TestData.attendCourse.id)

        flow.collect {
            Truth.assertThat(it).isEqualTo(TestData.attendCourse)
        }
    }

    @Test
    fun `getAsFlow should return null if not exist`() = runBlocking {
        val flow = repository.getAsFlow(0L)

        flow.collect {
            Truth.assertThat(it).isNull()
        }
    }

    @Test
    fun `getAllAsFlow should return AttendCourse list`() = runBlocking {
        val flow = repository.getAllAsFlow()

        flow.collect {
            Truth.assertThat(it).hasSize(3)
        }
    }

    @Test
    fun `getAllAsFlow_dayOfWeek should return AttendCourse list`() = runBlocking {
        val flow = repository.getAllAsFlow(TestData.attendCourse.dayOfWeek)

        flow.collect {
            Truth.assertThat(it).hasSize(3)
        }
    }

    @Test
    fun `get should return AttendCourse if exist`() = runBlocking {
        Truth.assertThat(repository.get(TestData.attendCourse.id)).isEqualTo(TestData.attendCourse)
    }

    @Test
    fun `get should return null if not exist`() = runBlocking {
        Truth.assertThat(repository.get(0L)).isNull()
    }

    @Test
    fun `getAll should return all AttendCourse`() = runBlocking {
        Truth.assertThat(repository.getAll()).hasSize(3)
    }

    @Test
    fun `add_attendCourse should insert AttendCourse to database`() = runBlocking {
        repository.add(TestData.attendCourse)

        verify { attendCourseDao.insert(TestData.attendCourse) }
    }

    @Test
    fun `add_attendCourse should return true if succeed`() = runBlocking {
        Truth.assertThat(repository.add(TestData.attendCourse)).isTrue()
    }

    @Test
    fun `add_attendCourse should return false if fail`() = runBlocking {
        every { attendCourseDao.insert(TestData.attendCourse) } returns 0L

        Truth.assertThat(repository.add(TestData.attendCourse)).isFalse()
    }

    @Test
    fun `add_lecture should insert AttendCourse to database`() = runBlocking {
        val lecture = object : Lecture {
            override val subject = "subject"
            override val instructor = "instructor"
            override val dayOfWeek = "月曜日"
            override val period = "1限"
        }
        val attendCourse = AttendCourse(
            subject = lecture.subject,
            instructor = lecture.instructor,
            dayOfWeek = DayOfWeek.MONDAY,
            period = 1,
            scheduleCode = "",
            credit = 0,
            category = "",
            type = AttendCourse.Type.USER
        )

        repository.add(lecture)

        verify { attendCourseDao.insert(listOf(attendCourse)) }
    }

    @Test
    fun `add_lecture should return true if succeed`() = runBlocking {
        val lecture = object : Lecture {
            override val subject = "subject"
            override val instructor = "instructor"
            override val dayOfWeek = "月曜日"
            override val period = "1限"
        }
        val attendCourse = AttendCourse(
            subject = lecture.subject,
            instructor = lecture.instructor,
            dayOfWeek = DayOfWeek.MONDAY,
            period = 1,
            scheduleCode = "",
            credit = 0,
            category = "",
            type = AttendCourse.Type.USER
        )

        every { attendCourseDao.insert(listOf(attendCourse)) } returns listOf(1L)

        Truth.assertThat(repository.add(lecture)).isTrue()
    }

    @Test
    fun `add_lecture should return true if fail`() = runBlocking {
        val lecture = object : Lecture {
            override val subject = "subject"
            override val instructor = "instructor"
            override val dayOfWeek = "月曜日"
            override val period = "1限"
        }
        val attendCourse = AttendCourse(
            subject = lecture.subject,
            instructor = lecture.instructor,
            dayOfWeek = DayOfWeek.MONDAY,
            period = 1,
            scheduleCode = "",
            credit = 0,
            category = "",
            type = AttendCourse.Type.USER
        )

        every { attendCourseDao.insert(listOf(attendCourse)) } returns emptyList()

        Truth.assertThat(repository.add(lecture)).isFalse()
    }

    @Test
    fun `update should update AttendCourse on database`() = runBlocking {
        repository.update(TestData.attendCourse)

        verify { attendCourseDao.update(TestData.attendCourse) }
    }

    @Test
    fun `update should return true if succeed`() = runBlocking {
        Truth.assertThat(repository.update(TestData.attendCourse)).isTrue()
    }

    @Test
    fun `update should return false if fail`() = runBlocking {
        every { attendCourseDao.update(TestData.attendCourse) } returns 0

        Truth.assertThat(repository.update(TestData.attendCourse)).isFalse()
    }

    @Test
    fun `updateAll should update AttendCourse list on database`() = runBlocking {
        repository.updateAll(listOf(TestData.attendCourse))

        verify { attendCourseDao.insertOrDelete(listOf(TestData.attendCourse)) }
    }

    @Test
    fun `remove_id should delete AttendCourse on database`() = runBlocking {
        repository.remove(1L)

        verify { attendCourseDao.delete(1L) }
    }

    @Test
    fun `remove_id should return true if succeed`() = runBlocking {
        Truth.assertThat(repository.remove(1L)).isTrue()
    }

    @Test
    fun `remove_id should return false if fail`() = runBlocking {
        Truth.assertThat(repository.remove(0L)).isFalse()
    }

    @Test
    fun `remove_subject should delete AttendCourse on database`() = runBlocking {
        repository.remove("subject")

        verify { attendCourseDao.delete("subject", AttendCourse.Type.USER) }
    }

    @Test
    fun `remove_subject should return true if succeed`() = runBlocking {
        Truth.assertThat(repository.remove("subject")).isTrue()
    }

    @Test
    fun `remove_subject should return false if fail`() = runBlocking {
        Truth.assertThat(repository.remove("unknown subject")).isFalse()
    }
}
