package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.MyCourseDao
import jp.kentan.studentportalplus.data.entity.Lecture
import jp.kentan.studentportalplus.data.entity.MyCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.data.vo.Period
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface MyCourseRepository {

    fun getAsFlow(id: Long): Flow<MyCourse?>

    fun getAllAsFlow(): Flow<List<MyCourse>>

    fun getAllAsFlow(dayOfWeek: DayOfWeek): Flow<List<MyCourse>>

    suspend fun get(id: Long): MyCourse?

    suspend fun getAll(): List<MyCourse>

    suspend fun add(myCourse: MyCourse): Boolean

    suspend fun add(lecture: Lecture): Boolean

    suspend fun update(myCourse: MyCourse): Boolean

    suspend fun updateAll(myCourseList: List<MyCourse>)

    suspend fun remove(id: Long): Boolean

    suspend fun remove(subject: String): Boolean
}

@ExperimentalCoroutinesApi
class DefaultMyCourseRepository(
    private val myCourseDao: MyCourseDao
) : MyCourseRepository {

    override fun getAsFlow(id: Long): Flow<MyCourse?> = myCourseDao.selectAsFlow(id)

    override fun getAllAsFlow(): Flow<List<MyCourse>> = myCourseDao.selectAsFlow().map { list ->
        list.sortedBy { it.subject }
            .sortedBy { it.isEditable }
            .sortedBy { it.period }
            .sortedBy { it.dayOfWeek }
    }.flowOn(Dispatchers.IO)

    override fun getAllAsFlow(dayOfWeek: DayOfWeek): Flow<List<MyCourse>> =
        myCourseDao.selectAsFlow(dayOfWeek)

    override suspend fun get(id: Long): MyCourse? = withContext(Dispatchers.IO) {
        myCourseDao.select(id)
    }

    override suspend fun getAll(): List<MyCourse> = myCourseDao.select()

    override suspend fun add(myCourse: MyCourse): Boolean = withContext(Dispatchers.IO) {
        val row = myCourseDao.insert(myCourse)
        return@withContext row > 0
    }

    override suspend fun add(lecture: Lecture): Boolean = withContext(Dispatchers.IO) {
        val dayOfWeek = DayOfWeek.similarOf(lecture.dayOfWeek) ?: return@withContext false
        val periodRange = if (dayOfWeek.hasPeriod) {
            Period.rangeOf(lecture.period)
        } else {
            Period.DEFAULT_RANGE
        }

        val myCourseList = periodRange.map { period ->
            MyCourse(
                subject = lecture.subject,
                instructor = lecture.instructor,
                dayOfWeek = dayOfWeek,
                period = period,
                scheduleCode = "",
                credit = 0,
                category = "",
                isEditable = true
            )
        }

        return@withContext myCourseDao.insert(myCourseList).isNotEmpty()
    }

    override suspend fun update(myCourse: MyCourse): Boolean = withContext(Dispatchers.IO) {
        val count = myCourseDao.update(myCourse)
        return@withContext count > 0
    }

    override suspend fun updateAll(myCourseList: List<MyCourse>) =
        myCourseDao.insertOrDelete(myCourseList)

    override suspend fun remove(id: Long): Boolean = withContext(Dispatchers.IO) {
        val count = myCourseDao.delete(id)
        return@withContext count > 0
    }

    override suspend fun remove(subject: String): Boolean = withContext(Dispatchers.IO) {
        val count = myCourseDao.delete(subject)
        return@withContext count > 0
    }
}
