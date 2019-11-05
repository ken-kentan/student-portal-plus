package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.LectureCancellationDao
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.calcAttendCourseType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface LectureCancellationRepository {
    fun getFlow(id: Long): Flow<LectureCancellation?>

    fun getListFlow(): Flow<List<LectureCancellation>>

    suspend fun setRead(id: Long)
}

class DefaultLectureCancellationRepository(
    private val lectureCancellationDao: LectureCancellationDao,
    attendCourseDao: AttendCourseDao,
    localPreferences: LocalPreferences
) : LectureCancellationRepository {

    private val subjectListFlow = attendCourseDao.getSubjectListFlow()
    private val similarSubjectThresholdFlow = localPreferences.similarSubjectThresholdFlow

    override fun getFlow(id: Long): Flow<LectureCancellation?> = combine(
        lectureCancellationDao.getFlow(id),
        subjectListFlow,
        similarSubjectThresholdFlow
    ) { lectureCancel, subjectList, threshold ->
        lectureCancel.copy(
            attendType = subjectList.calcAttendCourseType(
                lectureCancel.subject,
                threshold
            )
        )
    }.flowOn(Dispatchers.IO)

    override fun getListFlow(): Flow<List<LectureCancellation>> = combine(
        lectureCancellationDao.getListFlow(),
        subjectListFlow,
        similarSubjectThresholdFlow
    ) { lectureCancelList, subjectList, threshold ->
        lectureCancelList.map { info ->
            info.copy(attendType = subjectList.calcAttendCourseType(info.subject, threshold))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun setRead(id: Long) {
        withContext(Dispatchers.IO) {
            lectureCancellationDao.updateRead(id)
        }
    }
}
