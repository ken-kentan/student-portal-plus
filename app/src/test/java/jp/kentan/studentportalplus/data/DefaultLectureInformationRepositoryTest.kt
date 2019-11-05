package jp.kentan.studentportalplus.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jp.kentan.studentportalplus.LiveDataTestUtil
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.dao.FakeAttendCourseDao
import jp.kentan.studentportalplus.data.dao.FakeLectureInformationDao
import jp.kentan.studentportalplus.data.dao.LectureInformationDao
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class DefaultLectureInformationRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val localPreferences = mockk<LocalPreferences>()

    private val repository = DefaultLectureInformationRepository(
        FakeLectureInformationDao(),
        FakeAttendCourseDao(),
        localPreferences
    )

    @Test
    fun getObservable() {
        val observable = repository.getObservable(TestData.lectureInfo.id)

        assertThat(LiveDataTestUtil.getValue(observable)).isEqualTo(TestData.lectureInfo)
    }

    @Test
    fun getObservableList() {
        every {
            localPreferences.similarSubjectThresholdAsLiveData
        } returns MutableLiveData(0.8f)

        val observableList = repository.getObservableList()

        assertThat(LiveDataTestUtil.getValue(observableList))
            .hasSize(FakeLectureInformationDao.ALL_LIST_SIZE)
    }

    @Test
    fun markAsRead() = runBlocking {
        val dao = mockk<LectureInformationDao>()
        val repo = DefaultLectureInformationRepository(
            dao,
            FakeAttendCourseDao(),
            localPreferences
        )
        every { dao.updateAsRead(TestData.lectureInfo.id) } returns 1

        val isSuccess = repo.markAsRead(TestData.lectureInfo.id)

        verify {
            dao.updateAsRead(TestData.lectureInfo.id)
        }

        assertThat(isSuccess).isTrue()
    }
}