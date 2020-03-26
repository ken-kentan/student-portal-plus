package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.NoticeDao
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.data.vo.NoticeQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Calendar

interface NoticeRepository {

    fun getAsFlow(id: Long): Flow<Notice>

    fun getAllAsFlow(): Flow<List<Notice>>

    fun getAllAsFlow(queryFlow: Flow<NoticeQuery>): Flow<List<Notice>>

    suspend fun update(notice: Notice): Boolean

    suspend fun setRead(id: Long)
}

@ExperimentalCoroutinesApi
class DefaultNoticeRepository(
    private val noticeDao: NoticeDao
) : NoticeRepository {

    override fun getAsFlow(id: Long): Flow<Notice> = noticeDao.selectAsFlow(id)

    override fun getAllAsFlow(): Flow<List<Notice>> = noticeDao.selectAsFlow()

    override fun getAllAsFlow(queryFlow: Flow<NoticeQuery>): Flow<List<Notice>> = combine(
        noticeDao.selectAsFlow(),
        queryFlow
    ) { noticeList, query ->
        val calendar = Calendar.getInstance()
        val dateRangeTimeMillis = query.dateRange.createTimeInMillis(calendar)

        noticeList.filter { notice ->
            if (query.isUnread && notice.isRead) {
                return@filter false
            }
            if (query.isRead && !notice.isRead) {
                return@filter false
            }
            if (query.isFavorite && !notice.isFavorite) {
                return@filter false
            }
            if (query.dateRange != NoticeQuery.DateRange.ALL) {
                return@filter notice.createdDate.time >= dateRangeTimeMillis
            }
            if (query.textList.isNotEmpty()) {
                return@filter query.textList.any { notice.title.contains(it, true) }
            }

            return@filter true
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun update(notice: Notice): Boolean = withContext(Dispatchers.IO) {
        val count = noticeDao.update(notice)
        return@withContext count > 0
    }

    override suspend fun setRead(id: Long) {
        withContext(Dispatchers.IO) {
            noticeDao.updateRead(id)
        }
    }
}
