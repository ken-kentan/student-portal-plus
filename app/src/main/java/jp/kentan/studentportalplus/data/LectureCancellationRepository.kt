package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.LectureCancellationDao
import jp.kentan.studentportalplus.data.dao.MyCourseDao
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.data.vo.resolveMyCourseType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface LectureCancellationRepository {

    fun getAsFlow(id: Long): Flow<LectureCancellation?>

    fun getAllAsFlow(): Flow<List<LectureCancellation>>

    fun getAllAsFlow(queryFlow: Flow<LectureQuery>): Flow<List<LectureCancellation>>

    fun getAllFilteredByMyCourseAsFlow(): Flow<List<LectureCancellation>>

    suspend fun updateAll(lectureCancellationList: List<LectureCancellation>): List<LectureCancellation>

    suspend fun setRead(id: Long)
}

@ExperimentalCoroutinesApi
class DefaultLectureCancellationRepository(
    private val lectureCancellationDao: LectureCancellationDao,
    myCourseDao: MyCourseDao,
    preferences: Preferences
) : LectureCancellationRepository {

    private val myCourseListFlow = myCourseDao.selectAsFlow()
    private val similarSubjectThresholdFlow = preferences.similarSubjectThresholdFlow

    private val lectureCancelListFlow = combine(
        lectureCancellationDao.selectAsFlow(),
        myCourseListFlow,
        similarSubjectThresholdFlow
    ) { lectureCancelList, myCourseList, threshold ->
        lectureCancelList.map { lecture ->
            lecture.copy(
                myCourseType = myCourseList.resolveMyCourseType(
                    lecture.subject,
                    threshold
                )
            )
        }
    }

    override fun getAsFlow(id: Long): Flow<LectureCancellation?> = combine(
        lectureCancellationDao.selectAsFlow(id),
        myCourseListFlow,
        similarSubjectThresholdFlow
    ) { lectureCancel, myCourseList, threshold ->
        lectureCancel.copy(
            myCourseType = myCourseList.resolveMyCourseType(
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
            if (query.isMyCourse && !lecture.myCourseType.isMyCourse) {
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
            LectureQuery.Order.MY_COURSE -> filteredList.sortedByDescending {
                it.myCourseType.isMyCourse
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllFilteredByMyCourseAsFlow() = lectureCancelListFlow.map { lectureCancelList ->
        lectureCancelList.filter { it.myCourseType.isMyCourse }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateAll(lectureCancellationList: List<LectureCancellation>) =
        lectureCancellationDao.insertOrDelete(lectureCancellationList)

    override suspend fun setRead(id: Long) {
        withContext(Dispatchers.IO) {
            lectureCancellationDao.updateRead(id)
        }
    }
}
