package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.LectureInformationDao
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.calcAttendCourseType
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.vo.LectureQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface LectureInformationRepository {
    fun getFlow(id: Long): Flow<LectureInformation?>

    fun getListFlow(): Flow<List<LectureInformation>>

    fun getListFlow(queryFlow: Flow<LectureQuery>): Flow<List<LectureInformation>>

    suspend fun setRead(id: Long)

    suspend fun syncWithRemote(): List<LectureInformation>
}

@ExperimentalCoroutinesApi
class DefaultLectureInformationRepository(
    private val lectureInformationDao: LectureInformationDao,
    private val shibbolethClient: ShibbolethClient,
    attendCourseDao: AttendCourseDao,
    localPreferences: LocalPreferences
) : LectureInformationRepository {

    companion object {
        private const val LECTURE_INFO_URL =
            "https://portal.student.kit.ac.jp/ead/?c=lecture_information"
    }

    private val subjectListFlow = attendCourseDao.getSubjectListFlow()
    private val similarSubjectThresholdFlow = localPreferences.similarSubjectThresholdFlow
    private val lectureInfoListFlow = combine(
        lectureInformationDao.getListFlow(),
        subjectListFlow,
        similarSubjectThresholdFlow
    ) { lectureInfoList, subjectList, threshold ->
        lectureInfoList.map { lecture ->
            lecture.copy(attendType = subjectList.calcAttendCourseType(lecture.subject, threshold))
        }
    }

    override fun getFlow(id: Long) = combine(
        lectureInformationDao.getFlow(id),
        subjectListFlow,
        similarSubjectThresholdFlow
    ) { lectureInfo, subjectList, threshold ->
        lectureInfo?.copy(
            attendType = subjectList.calcAttendCourseType(
                lectureInfo.subject,
                threshold
            )
        )
    }.flowOn(Dispatchers.IO)

    override fun getListFlow() = lectureInfoListFlow.flowOn(Dispatchers.IO)

    override fun getListFlow(queryFlow: Flow<LectureQuery>) = combine(
        lectureInfoListFlow,
        queryFlow
    ) { lectureInfoList, query ->
        val filteredList = lectureInfoList.filter { lecture ->
            if (query.isUnread && lecture.isRead) {
                return@filter false
            }
            if (query.isRead && !lecture.isRead) {
                return@filter false
            }
            if (query.isAttend && !lecture.attendType.isAttend) {
                return@filter false
            }
            if (query.textList.isNotEmpty()) {
                return@filter query.textList.any {
                    lecture.subject.contains(it, ignoreCase = true)
                        || lecture.instructor.contains(it, ignoreCase = true)
                }
            }

            return@filter true
        }

        return@combine when (query.order) {
            LectureQuery.Order.UPDATED_DATE -> filteredList
            LectureQuery.Order.ATTEND_CLASS -> filteredList.sortedByDescending {
                it.attendType.isAttend
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun setRead(id: Long) {
        withContext(Dispatchers.IO) {
            lectureInformationDao.updateRead(id)
        }
    }

    override suspend fun syncWithRemote(): List<LectureInformation> = withContext(Dispatchers.IO) {
        val document = shibbolethClient.fetch(LECTURE_INFO_URL)
        val lectureInfoList = DocumentParser.parseLectureInformation(document)
        lectureInformationDao.updateAll(lectureInfoList)
    }
}
