package jp.kentan.studentportalplus.data.dao

import android.util.Log
import androidx.room.*
import jp.kentan.studentportalplus.data.entity.LectureInformation
import kotlinx.coroutines.flow.Flow

@Dao
interface LectureInformationDao {

    @Query("SELECT * FROM lecture_infos WHERE _id = :id")
    fun getFlow(id: Long): Flow<LectureInformation?>

    @Query("SELECT * FROM lecture_infos ORDER BY updated_date DESC, subject")
    fun getListFlow(): Flow<List<LectureInformation>>

    @Transaction
    fun updateAll(lectureInfoList: List<LectureInformation>): List<LectureInformation> {
        val insertList = mutableListOf<LectureInformation>()

        lectureInfoList.forEach { info ->
            val id = insert(info)

            Log.d("LectureInformationDao", "$id: ${info.subject}")
            if (id > 0) {
                insertList.add(info)
            }
        }

        deleteNotInHash(lectureInfoList.map { it.hash })

        return insertList
    }

    @Query("UPDATE lecture_infos SET is_read = 1 WHERE _id = :id AND is_read = 0")
    fun updateRead(id: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(lectureInfo: LectureInformation): Long

    @Query("DELETE FROM lecture_infos WHERE hash NOT IN (:hash)")
    fun deleteNotInHash(hash: List<Long>)

}
