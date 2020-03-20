package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.NoticeDao
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.vo.NoticeQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Calendar

interface NoticeRepository {
    fun getFlow(id: Long): Flow<Notice>

    fun getListFlow(): Flow<List<Notice>>

    fun getListFlow(queryFlow: Flow<NoticeQuery>): Flow<List<Notice>>

    suspend fun update(notice: Notice): Boolean

    suspend fun setRead(id: Long)

    suspend fun syncWithRemote(): List<Notice>
}

@ExperimentalCoroutinesApi
class DefaultNoticeRepository(
    private val noticeDao: NoticeDao,
    private val shibbolethClient: ShibbolethClient
) : NoticeRepository {

    companion object {
        private const val NOTICE_URL = "https://portal.student.kit.ac.jp"
    }

    override fun getFlow(id: Long): Flow<Notice> = noticeDao.getFlow(id)

    override fun getListFlow(): Flow<List<Notice>> = noticeDao.getListFlow()

    override fun getListFlow(queryFlow: Flow<NoticeQuery>): Flow<List<Notice>> = combine(
        noticeDao.getListFlow(),
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

    override suspend fun syncWithRemote(): List<Notice> = withContext(Dispatchers.IO) {
        val document = shibbolethClient.fetch(NOTICE_URL)
        val noticeList = DocumentParser.parseNotice(document)
        noticeDao.updateAll(noticeList)
    }
}
