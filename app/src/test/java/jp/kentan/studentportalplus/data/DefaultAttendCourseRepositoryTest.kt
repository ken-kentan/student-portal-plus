package jp.kentan.studentportalplus.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jp.kentan.studentportalplus.LiveDataTestUtil
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.FakeAttendCourseDao
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultAttendCourseRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository = DefaultAttendCourseRepository(FakeAttendCourseDao())

    @Test
    fun getObservable() {
        val observable = repository.getObservable(TestData.attendCourse.id)

        assertThat(LiveDataTestUtil.getValue(observable)).isEqualTo(TestData.attendCourse)
    }

    @Test
    fun getObservableList() {
        val observableList = repository.getObservableList()

        assertThat(LiveDataTestUtil.getValue(observableList))
            .hasSize(FakeAttendCourseDao.ALL_LIST_SIZE)
    }

    @Test
    fun getObservableList_dayOfWeek() {
        val observableList = repository.getObservableList(DayOfWeek.MONDAY)

        assertThat(LiveDataTestUtil.getValue(observableList))
            .hasSize(FakeAttendCourseDao.DAY_OF_WEEK_LIST_SIZE)
    }

    @Test
    fun get() = runBlocking {
        val attendCourse = repository.get(TestData.attendCourse.id)

        assertThat(attendCourse).isEqualTo(TestData.attendCourse)
    }

    @Test
    fun add() = runBlocking {
        val dao = mockk<AttendCourseDao>()
        val repo = DefaultAttendCourseRepository(dao)
        every { dao.insert(TestData.attendCourse) } returns 1

        val isSuccess = repo.add(TestData.attendCourse)

        verify {
            dao.insert(TestData.attendCourse)
        }

        assertThat(isSuccess).isTrue()
    }

    @Test
    fun update() = runBlocking {
        val dao = mockk<AttendCourseDao>()
        val repo = DefaultAttendCourseRepository(dao)
        every { dao.update(TestData.attendCourse) } returns 1

        val isSuccess = repo.update(TestData.attendCourse)

        verify {
            dao.update(TestData.attendCourse)
        }

        assertThat(isSuccess).isTrue()
    }

    @Test
    fun delete() = runBlocking {
        val dao = mockk<AttendCourseDao>()
        val repo = DefaultAttendCourseRepository(dao)
        every { dao.delete(TestData.attendCourse.id) } returns 1

        val isSuccess = repo.remove(TestData.attendCourse.id)

        verify {
            dao.delete(TestData.attendCourse.id)
        }

        assertThat(isSuccess).isTrue()
    }
}