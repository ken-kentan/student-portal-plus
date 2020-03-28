package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.LectureCancellationDao
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.vo.LectureQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface LectureCancellationRepository {

    fun getAsFlow(id: Long): Flow<LectureCancellation?>

    fun getAllAsFlow(): Flow<List<LectureCancellation>>

    fun getAllAsFlow(queryFlow: Flow<LectureQuery>): Flow<List<LectureCancellation>>

    suspend fun updateAll(lectureCancellationList: List<LectureCancellation>): List<LectureCancellation>

    suspend fun setRead(id: Long)
}

@ExperimentalCoroutinesApi
class DefaultLectureCancellationRepository(
    private val lectureCancellationDao: LectureCancellationDao,
    attendCourseDao: AttendCourseDao,
    preferences: Preferences
) : LectureCancellationRepository {

    private val attendCourseListFlow = attendCourseDao.selectAsFlow()
    private val similarSubjectThresholdFlow = preferences.similarSubjectThresholdFlow

    private val lectureCancelListFlow = combine(
        lectureCancellationDao.selectAsFlow(),
        attendCourseListFlow,
        similarSubjectThresholdFlow
    ) { lectureCancelList, attendCourseList, threshold ->
        lectureCancelList.map { lecture ->
            lecture.copy(
                attendType = attendCourseList.resolveAttendCourseType(
                    lecture.subject,
                    threshold
                )
            )
        }
    }

    override fun getAsFlow(id: Long): Flow<LectureCancellation?> = combine(
        lectureCancellationDao.selectAsFlow(id),
        attendCourseListFlow,
        similarSubjectThresholdFlow
    ) { lectureCancel, attendCourseList, threshold ->
        lectureCancel.copy(
            attendType = attendCourseList.resolveAttendCourseType(
                lectureCancel.subject,
                threshold
            )
        )
    }.flowOn(Dispatchers.IO)

    override fun getAllAsFlow() = lectureCancelListFlow.flowOn(Dispatchers.IO)

    override fun getAllAsFlow(queryFlow: Flow<LectureQuery>) = combine(
        lectureCancelListFlow,
        queryFlow
    ) { lectureCancelList, query ->
        val filteredList = lectureCancelList.filter { lecture ->
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
                    lecture.subject.contains(it, ignoreCase = true) || lecture.instructor.contains(
                        it,
                        ignoreCase = true
                    )
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

    override suspend fun updateAll(lectureCancellationList: List<LectureCancellation>) =
        lectureCancellationDao.insertOrDelete(lectureCancellationList)

    override suspend fun setRead(id: Long) {
        withContext(Dispatchers.IO) {
            lectureCancellationDao.updateRead(id)
        }
    }
}
