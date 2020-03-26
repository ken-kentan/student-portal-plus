package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.AttendCourseDao
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.Lecture
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface AttendCourseRepository {

    fun getAsFlow(id: Long): Flow<AttendCourse?>

    fun getAllAsFlow(): Flow<List<AttendCourse>>

    fun getAllAsFlow(dayOfWeek: DayOfWeek): Flow<List<AttendCourse>>

    suspend fun get(id: Long): AttendCourse?

    suspend fun add(attendCourse: AttendCourse): Boolean

    suspend fun add(lecture: Lecture): Boolean

    suspend fun update(attendCourse: AttendCourse): Boolean

    suspend fun updateAll(attendCourseList: List<AttendCourse>)

    suspend fun remove(id: Long): Boolean

    suspend fun remove(subject: String): Boolean
}

@ExperimentalCoroutinesApi
class DefaultAttendCourseRepository(
    private val attendCourseDao: AttendCourseDao
) : AttendCourseRepository {

    override fun getAsFlow(id: Long): Flow<AttendCourse?> = attendCourseDao.selectAsFlow(id)

    override fun getAllAsFlow(): Flow<List<AttendCourse>> =
        attendCourseDao.selectAsFlow().map { list ->
            list.sortedBy { it.subject }
                .sortedBy { it.type == AttendCourse.Type.USER }
                .sortedBy { it.period }
                .sortedBy { it.dayOfWeek }
        }.flowOn(Dispatchers.IO)

    override fun getAllAsFlow(dayOfWeek: DayOfWeek): Flow<List<AttendCourse>> =
        attendCourseDao.selectAsFlow(dayOfWeek)

    override suspend fun get(id: Long): AttendCourse? = withContext(Dispatchers.IO) {
        attendCourseDao.select(id)
    }

    override suspend fun add(attendCourse: AttendCourse): Boolean = withContext(Dispatchers.IO) {
        val row = attendCourseDao.insert(attendCourse)
        return@withContext row > 0
    }

    override suspend fun add(lecture: Lecture): Boolean = withContext(Dispatchers.IO) {
        val dayOfWeek = DayOfWeek.similarOf(lecture.dayOfWeek) ?: return@withContext false
        val periodRange = if (dayOfWeek.hasPeriod) {
            Period.rangeOf(lecture.period)
        } else {
            IntRange(0, 0)
        }

        val attendCourseList = periodRange.map { period ->
            AttendCourse(
                subject = lecture.subject,
                instructor = lecture.instructor,
                dayOfWeek = dayOfWeek,
                period = period,
                scheduleCode = "",
                credit = 0,
                category = "",
                type = AttendCourse.Type.USER
            )
        }

        return@withContext attendCourseDao.insert(attendCourseList).isNotEmpty()
    }

    override suspend fun update(attendCourse: AttendCourse): Boolean = withContext(Dispatchers.IO) {
        val count = attendCourseDao.update(attendCourse)
        return@withContext count > 0
    }

    override suspend fun updateAll(attendCourseList: List<AttendCourse>) =
        attendCourseDao.insertOrDelete(attendCourseList)

    override suspend fun remove(id: Long): Boolean = withContext(Dispatchers.IO) {
        val count = attendCourseDao.delete(id)
        return@withContext count > 0
    }

    override suspend fun remove(subject: String): Boolean = withContext(Dispatchers.IO) {
        val count = attendCourseDao.delete(subject, AttendCourse.Type.USER)
        return@withContext count > 0
    }
}
