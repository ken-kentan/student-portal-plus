package jp.kentan.studentportalplus.data

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.dao.FakeAttendCourseDao
import jp.kentan.studentportalplus.data.dao.FakeLectureInformationDao
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DefaultLectureInformationRepositoryTest {

    private val localPreferences = mockk<LocalPreferences>().apply {
        every {
            similarSubjectThresholdFlow
        } returns flowOf(0.8f)
    }

    private val repository = DefaultLectureInformationRepository(
        FakeLectureInformationDao(),
        FakeAttendCourseDao(),
        localPreferences
    )

//    @Before
//    fun setUp() {
//        every {
//            localPreferences.similarSubjectThresholdFlow
//        } returns flowOf(0.8f)
//    }

    @Test
    fun getFlow() = runBlocking {
        every {
            localPreferences.similarSubjectThresholdFlow
        } returns flowOf(0.8f)

        val flow = repository.getFlow(TestData.lectureInfo.id)

        flow.collect {
            assertThat(it).isEqualTo(TestData.lectureInfo)
        }
    }

    @Test
    fun getListFlow() = runBlocking {
        val flow = repository.getListFlow()

        flow.collect {
            assertThat(it).hasSize(FakeLectureInformationDao.ALL_LIST_SIZE)
        }
    }

    @Test
    fun setRead() = runBlocking {
        val lectureInfoDao = spyk<FakeLectureInformationDao>()

        val repo = DefaultLectureInformationRepository(
            lectureInfoDao,
            FakeAttendCourseDao(),
            localPreferences
        )

        repo.setRead(TestData.lectureInfo.id)

        verify {
            lectureInfoDao.updateRead(TestData.lectureInfo.id)
        }
    }
}
