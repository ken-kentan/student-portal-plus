package jp.kentan.studentportalplus.data.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import kotlinx.coroutines.flow.Flow

@Dao
interface LectureCancellationDao {

    @Query("SELECT * FROM lecture_cancels WHERE _id = :id")
    fun getFlow(id: Long): Flow<LectureCancellation>

    @Query("SELECT * FROM lecture_cancels ORDER BY created_date DESC, subject")
    fun getListFlow(): Flow<List<LectureCancellation>>

    @Transaction
    fun updateAll(lectureCancelList: List<LectureCancellation>): List<LectureCancellation> {
        val insertList = mutableListOf<LectureCancellation>()

        lectureCancelList.forEach { cancel ->
            val id = insert(cancel)

            Log.d("LectureCancellationDao", "$id: ${cancel.subject}")
            if (id > 0) {
                insertList.add(cancel.copy(id = id))
            }
        }

        deleteNotInHash(lectureCancelList.map { it.hash })

        return insertList
    }

    @Query("UPDATE lecture_cancels SET is_read = 1 WHERE _id = :id AND is_read = 0")
    fun updateRead(id: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(lectureInfo: LectureCancellation): Long

    @Query("DELETE FROM lecture_cancels WHERE hash NOT IN (:hash)")
    fun deleteNotInHash(hash: List<Long>)
}
