package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.LectureInformationDao
import jp.kentan.studentportalplus.data.dao.MyCourseDao
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.vo.LectureQuery
import jp.kentan.studentportalplus.data.vo.resolveMyCourseType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface LectureInformationRepository {

    fun getAsFlow(id: Long): Flow<LectureInformation?>

    fun getAllAsFlow(): Flow<List<LectureInformation>>

    fun getAllAsFlow(queryFlow: Flow<LectureQuery>): Flow<List<LectureInformation>>

    fun getAllFilteredByMyCourseAsFlow(): Flow<List<LectureInformation>>

    suspend fun updateAll(lectureInformationList: List<LectureInformation>): List<LectureInformation>

    suspend fun setRead(id: Long)
}

@ExperimentalCoroutinesApi
class DefaultLectureInformationRepository(
    private val lectureInformationDao: LectureInformationDao,
    myCourseDao: MyCourseDao,
    preferences: Preferences
) : LectureInformationRepository {

    private val myCourseListFlow = myCourseDao.selectAsFlow()
    private val similarSubjectThresholdFlow = preferences.similarSubjectThresholdFlow

    private val lectureInfoListFlow = combine(
        lectureInformationDao.selectAsFlow(),
        myCourseListFlow,
        similarSubjectThresholdFlow
    ) { lectureInfoList, myCourseList, threshold ->
        lectureInfoList.map { lecture ->
            lecture.copy(
                myCourseType = myCourseList.resolveMyCourseType(
                    lecture.subject,
                    threshold
                )
            )
        }
    }

    override fun getAsFlow(id: Long) = combine(
        lectureInformationDao.selectAsFlow(id),
        myCourseListFlow,
        similarSubjectThresholdFlow
    ) { lectureInfo, myCourseList, threshold ->
        lectureInfo?.copy(
            myCourseType = myCourseList.resolveMyCourseType(
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
            if (query.isMyCourse && !lecture.myCourseType.isMyCourse) {
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
            LectureQuery.Order.MY_COURSE -> filteredList.sortedByDescending {
                it.myCourseType.isMyCourse
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllFilteredByMyCourseAsFlow() = lectureInfoListFlow.map { lectureInfoList ->
        lectureInfoList.filter { it.myCourseType.isMyCourse }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateAll(lectureInformationList: List<LectureInformation>) =
        lectureInformationDao.insertOrDelete(lectureInformationList)

    override suspend fun setRead(id: Long) {
        withContext(Dispatchers.IO) {
            lectureInformationDao.updateRead(id)
        }
    }
}
