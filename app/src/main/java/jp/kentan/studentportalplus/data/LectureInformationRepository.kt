package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.LectureInformationDao
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.calcAttendCourseType
import jp.kentan.studentportalplus.data.vo.LectureQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface LectureInformationRepository {
    fun getFlow(id: Long): Flow<LectureInformation?>

    fun getListFlow(): Flow<List<LectureInformation>>

    fun getListFlow(query: LectureQuery): Flow<List<LectureInformation>>

    suspend fun setRead(id: Long)
}

class DefaultLectureInformationRepository(
    private val lectureInformationDao: LectureInformationDao,
    attendCourseDao: AttendCourseDao,
    localPreferences: LocalPreferences
) : LectureInformationRepository {

    private val subjectListFlow = attendCourseDao.getSubjectListFlow()
    private val similarSubjectThresholdFlow = localPreferences.similarSubjectThresholdFlow

    override fun getFlow(id: Long): Flow<LectureInformation?> = combine(
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

    override fun getListFlow(): Flow<List<LectureInformation>> = combine(
        lectureInformationDao.getListFlow(),
        subjectListFlow,
        similarSubjectThresholdFlow
    ) { lectureInfoList, subjectList, threshold ->
        lectureInfoList.map { info ->
            info.copy(attendType = subjectList.calcAttendCourseType(info.subject, threshold))
        }
    }.flowOn(Dispatchers.IO)

    override fun getListFlow(query: LectureQuery): Flow<List<LectureInformation>> = combine(
        lectureInformationDao.getListFlow(),
        subjectListFlow,
        similarSubjectThresholdFlow
    ) { lectureInfoList, subjectList, threshold ->
        lectureInfoList.map { info ->
            info.copy(attendType = subjectList.calcAttendCourseType(info.subject, threshold))
        }.filter { lectureInfo ->
            if (query.textList.isNotEmpty()) {
                return@filter query.textList.any {
                    lectureInfo.subject.contains(it) || lectureInfo.instructor.contains(it)
                }
            }
            return@filter true
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun setRead(id: Long) {
        withContext(Dispatchers.IO) {
            lectureInformationDao.updateRead(id)
        }
    }
}
