package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.LectureInformationDao
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.vo.LectureQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface LectureInformationRepository {

    fun getAsFlow(id: Long): Flow<LectureInformation?>

    fun getAllAsFlow(): Flow<List<LectureInformation>>

    fun getAllAsFlow(queryFlow: Flow<LectureQuery>): Flow<List<LectureInformation>>

    suspend fun setRead(id: Long)
}

@ExperimentalCoroutinesApi
class DefaultLectureInformationRepository(
    private val lectureInformationDao: LectureInformationDao,
    attendCourseDao: AttendCourseDao,
    localPreferences: LocalPreferences
) : LectureInformationRepository, LectureRepository(attendCourseDao, localPreferences) {

    private val lectureInfoListFlow = combine(
        lectureInformationDao.selectAsFlow(),
        attendCourseListFlow,
        similarSubjectThresholdFlow
    ) { lectureInfoList, attendCourseList, threshold ->
        lectureInfoList.map { lecture ->
            lecture.copy(
                attendType = attendCourseList.calcAttendCourseType(
                    lecture.subject,
                    threshold
                )
            )
        }
    }

    override fun getAsFlow(id: Long) = combine(
        lectureInformationDao.selectAsFlow(id),
        attendCourseListFlow,
        similarSubjectThresholdFlow
    ) { lectureInfo, attendCourseList, threshold ->
        lectureInfo?.copy(
            attendType = attendCourseList.calcAttendCourseType(
                lectureInfo.subject,
                threshold
            )
        )
    }.flowOn(Dispatchers.IO)

    override fun getAllAsFlow() = lectureInfoListFlow.flowOn(Dispatchers.IO)

    override fun getAllAsFlow(queryFlow: Flow<LectureQuery>) = combine(
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
                    lecture.subject.contains(it, ignoreCase = true) ||
                        lecture.instructor.contains(it, ignoreCase = true)
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
}
