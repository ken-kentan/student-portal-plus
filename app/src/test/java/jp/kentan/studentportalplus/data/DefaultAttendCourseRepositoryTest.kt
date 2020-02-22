package jp.kentan.studentportalplus.data

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.FakeAttendCourseDao
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.Lecture
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultAttendCourseRepositoryTest {

    private val repository = DefaultAttendCourseRepository(FakeAttendCourseDao(), mockk())

    @Test
    fun getFlow() = runBlocking {
        val flow = repository.getFlow(TestData.attendCourse.id)

        flow.collect {
            assertThat(it).isEqualTo(TestData.attendCourse)
        }
    }

    @Test
    fun getListFlow() = runBlocking {
        val flow = repository.getListFlow()

        flow.collect {
            assertThat(it).hasSize(FakeAttendCourseDao.ALL_LIST_SIZE)
        }
    }

    @Test
    fun getListFlow_dayOfWeek() = runBlocking {
        val flow = repository.getListFlow(DayOfWeek.MONDAY)

        flow.collect {
            assertThat(it).hasSize(FakeAttendCourseDao.DAY_OF_WEEK_LIST_SIZE)
        }
    }

    @Test
    fun get() = runBlocking {
        val attendCourse = repository.get(TestData.attendCourse.id)

        assertThat(attendCourse).isEqualTo(TestData.attendCourse)
    }

    @Test
    fun add() = runBlocking {
        val dao = mockk<AttendCourseDao>()
        val repo = DefaultAttendCourseRepository(dao, mockk())
        every { dao.insert(TestData.attendCourse) } returns 1

        val isSuccess = repo.add(TestData.attendCourse)

        verify {
            dao.insert(TestData.attendCourse)
        }

        assertThat(isSuccess).isTrue()
    }

    @Test
    fun add_lecture() = runBlocking {
        val dao = mockk<AttendCourseDao>()
        val repo = DefaultAttendCourseRepository(dao, mockk())
        every { dao.insertAll(any()) } returns listOf(1)

        val lecture = object : Lecture {
            override val subject = "subject"
            override val instructor = "instructor"
            override val dayOfWeek = "月曜日"
            override val period = "1"
        }

        val isSuccess = repo.add(lecture)

        verify {
            dao.insertAll(any())
        }

        assertThat(isSuccess).isTrue()
    }

    @Test
    fun update() = runBlocking {
        val dao = mockk<AttendCourseDao>()
        val repo = DefaultAttendCourseRepository(dao, mockk())
        every { dao.update(TestData.attendCourse) } returns 1

        val isSuccess = repo.update(TestData.attendCourse)

        verify {
            dao.update(TestData.attendCourse)
        }

        assertThat(isSuccess).isTrue()
    }

    @Test
    fun remove() = runBlocking {
        val dao = mockk<AttendCourseDao>()
        val repo = DefaultAttendCourseRepository(dao, mockk())
        every { dao.delete(TestData.attendCourse.id) } returns 1

        val isSuccess = repo.remove(TestData.attendCourse.id)

        verify {
            dao.delete(TestData.attendCourse.id)
        }

        assertThat(isSuccess).isTrue()
    }

    @Test
    fun remove_subject() = runBlocking {
        val dao = mockk<AttendCourseDao>()
        val repo = DefaultAttendCourseRepository(dao, mockk())
        every { dao.delete("subject", AttendCourse.Type.USER) } returns 1

        val isSuccess = repo.remove("subject")

        verify {
            dao.delete("subject", AttendCourse.Type.USER)
        }

        assertThat(isSuccess).isTrue()
    }

    @Test
    fun syncWithRemote() = runBlocking {
        val list = listOf(TestData.attendCourse)

        val dao = mockk<AttendCourseDao>()
        val client = mockk<ShibbolethClient>()

        mockkObject(DocumentParser)

        val repo = DefaultAttendCourseRepository(dao, client)
        every { dao.updateAll(list) } returns Unit
        every { client.fetch(any()) } returns mockk()
        every { DocumentParser.parseAttendCourse(any()) } returns list

        repo.syncWithRemote()

        verify {
            client.fetch(any())
        }
        verify {
            DocumentParser.parseAttendCourse(any())
        }
        verify {
            dao.updateAll(list)
        }
    }
}
