package jp.kentan.studentportalplus.data.dao

import android.util.Log
import androidx.room.*
import jp.kentan.studentportalplus.data.entity.Notice
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {

    @Query("SELECT * FROM notices WHERE _id = :id")
    fun getFlow(id: Long): Flow<Notice>

    @Query("SELECT * FROM notices ORDER BY created_date DESC, _id")
    fun getListFlow(): Flow<List<Notice>>

    @Transaction
    fun updateAll(noticeList: List<Notice>): List<Notice> {
        val insertList = mutableListOf<Notice>()

        noticeList.forEach { notice ->
            val id = insert(notice)

            Log.d("NoticeDao", "$id: ${notice.title}")
            if (id > 0) {
                insertList.add(notice.copy(id = id))
            }
        }

        // Delete old notices
        deleteNotInHash(noticeList.map { it.hash })

        return insertList
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(notice: Notice): Long

    @Update
    fun update(notice: Notice): Int

    @Query("UPDATE notices SET is_read = 1 WHERE _id = :id AND is_read = 0")
    fun updateRead(id: Long): Int

    @Query("DELETE FROM notices WHERE is_favorite = 0 AND hash NOT IN (:hash)")
    fun deleteNotInHash(hash: List<Long>)

}
