package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.dao.LectureCancellationDao
import jp.kentan.studentportalplus.data.dao.LectureInformationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

interface SubjectRepository {

    fun getListFlow(): Flow<List<String>>
}

@ExperimentalCoroutinesApi
class DefaultSubjectRepository(
    private val lectureInformationDao: LectureInformationDao,
    private val lectureCancellationDao: LectureCancellationDao,
    private val attendCourseDao: AttendCourseDao
) : SubjectRepository {

    override fun getListFlow(): Flow<List<String>> = combine(
        lectureInformationDao.getListFlow(),
        lectureCancellationDao.getListFlow(),
        attendCourseDao.getListFlow()
    ) { lectureInfoList, lectureCancelList, attendCourseList ->
        mutableListOf<String>().apply {
            addAll(lectureInfoList.map { it.subject })
            addAll(lectureCancelList.map { it.subject })
            addAll(attendCourseList.map { it.subject })
        }.distinct().sorted()
    }.flowOn(Dispatchers.IO)
}
