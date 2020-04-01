package jp.kentan.studentportalplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import jp.kentan.studentportalplus.data.entity.MyCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.flow.Flow

@Dao
interface MyCourseDao {

    @Query("SELECT * FROM my_courses WHERE _id = :id")
    fun selectAsFlow(id: Long): Flow<MyCourse?>

    @Query("SELECT * FROM my_courses")
    fun selectAsFlow(): Flow<List<MyCourse>>

    @Query("SELECT * FROM my_courses WHERE day_of_week = :dayOfWeek ORDER BY period, subject")
    fun selectAsFlow(dayOfWeek: DayOfWeek): Flow<List<MyCourse>>

    @Query("SELECT * FROM my_courses WHERE _id = :id")
    fun select(id: Long): MyCourse?

    @Query("SELECT * FROM my_courses")
    fun select(): List<MyCourse>

    @Transaction
    fun insertOrDelete(myCourseList: List<MyCourse>) {
        insertIgnore(myCourseList)

        deleteNotInHash(myCourseList.map { it.hash })
    }

    @Insert
    fun insert(myCourse: MyCourse): Long

    @Insert
    fun insert(myCourseList: List<MyCourse>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnore(myCourseList: List<MyCourse>)

    @Update
    fun update(myCourse: MyCourse): Int

    @Query("DELETE FROM my_courses WHERE _id = :id")
    fun delete(id: Long): Int

    @Query("DELETE FROM my_courses WHERE is_editable = 1 AND subject = :subject")
    fun delete(subject: String): Int

    @Query("DELETE FROM my_courses WHERE is_editable = 0 AND hash NOT IN (:hash)")
    fun deleteNotInHash(hash: List<Long>)
}
