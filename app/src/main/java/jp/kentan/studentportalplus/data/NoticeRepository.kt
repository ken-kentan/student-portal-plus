package jp.kentan.studentportalplus.data

import jp.kentan.studentportalplus.data.dao.NoticeDao
import jp.kentan.studentportalplus.data.entity.Notice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface NoticeRepository {
    fun getFlow(id: Long): Flow<Notice>

    fun getListFlow(): Flow<List<Notice>>

    suspend fun update(notice: Notice): Boolean

    suspend fun setRead(id: Long)
}

class DefaultNoticeRepository(
    private val noticeDao: NoticeDao
) : NoticeRepository {

    override fun getFlow(id: Long): Flow<Notice> = noticeDao.getFlow(id)

    override fun getListFlow(): Flow<List<Notice>> = noticeDao.getListFlow()

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