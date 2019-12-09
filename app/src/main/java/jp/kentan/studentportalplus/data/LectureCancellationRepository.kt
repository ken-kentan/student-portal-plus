package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.LectureCancellationDao
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.calcAttendCourseType
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.vo.LectureQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface LectureCancellationRepository {
    fun getFlow(id: Long): Flow<LectureCancellation?>

    fun getListFlow(): Flow<List<LectureCancellation>>

    fun getListFlow(queryFlow: Flow<LectureQuery>): Flow<List<LectureCancellation>>

    suspend fun setRead(id: Long)

    suspend fun syncWithRemote(): List<LectureCancellation>
}

@ExperimentalCoroutinesApi
class DefaultLectureCancellationRepository(
    private val lectureCancellationDao: LectureCancellationDao,
    private val shibbolethClient: ShibbolethClient,
    attendCourseDao: AttendCourseDao,
    localPreferences: LocalPreferences
) : LectureCancellationRepository {

    companion object {
        private const val LECTURE_CANCEL_URL =
            "https://portal.student.kit.ac.jp/ead/?c=lecture_cancellation"
    }

    private val subjectListFlow = attendCourseDao.getSubjectListFlow()
    private val similarSubjectThresholdFlow = localPreferences.similarSubjectThresholdFlow
    private val lectureCancelListFlow = combine(
        lectureCancellationDao.getListFlow(),
        subjectListFlow,
        similarSubjectThresholdFlow
    ) { lectureCancel, subjectList, threshold ->
        lectureCancel.map { lecture ->
            lecture.copy(attendType = subjectList.calcAttendCourseType(lecture.subject, threshold))
        }
    }

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

    override fun getListFlow() = lectureCancelListFlow.flowOn(Dispatchers.IO)

    override fun getListFlow(queryFlow: Flow<LectureQuery>) = combine(
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
            lectureCancellationDao.updateRead(id)
        }
    }

    override suspend fun syncWithRemote(): List<LectureCancellation> = withContext(Dispatchers.IO) {
        val document = shibbolethClient.fetch(LECTURE_CANCEL_URL)
        val lectureCancelList = DocumentParser.parseLectureCancellation(document)
        lectureCancellationDao.updateAll(lectureCancelList)
    }
}
