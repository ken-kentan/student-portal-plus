package jp.kentan.studentportalplus.data

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.dao.FakeAttendCourseDao
import jp.kentan.studentportalplus.data.dao.FakeLectureInformationDao
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.domain.DocumentParser
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
        mockk(),
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
            mockk(),
            FakeAttendCourseDao(),
            localPreferences
        )

        repo.setRead(TestData.lectureInfo.id)

        verify {
            lectureInfoDao.updateRead(TestData.lectureInfo.id)
        }
    }

    @Test
    fun syncWithRemote() = runBlocking {
        val list = listOf(TestData.lectureInfo)

        val lectureInfoDao = spyk<FakeLectureInformationDao>()
        val client = mockk<ShibbolethClient>()

        mockkObject(DocumentParser)

        val repo = DefaultLectureInformationRepository(
            lectureInfoDao,
            client,
            FakeAttendCourseDao(),
            localPreferences
        )
        every { client.fetch(any()) } returns mockk()
        every { DocumentParser.parseLectureInformation(any()) } returns list

        repo.syncWithRemote()

        verify {
            client.fetch(any())
        }
        verify {
            DocumentParser.parseLectureInformation(any())
        }
        verify {
            lectureInfoDao.updateAll(list)
        }
    }
}
