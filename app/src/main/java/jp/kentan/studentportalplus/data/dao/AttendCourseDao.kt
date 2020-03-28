package jp.kentan.studentportalplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendCourseDao {

    @Query("SELECT * FROM attend_courses WHERE _id = :id")
    fun selectAsFlow(id: Long): Flow<AttendCourse?>

    @Query("SELECT * FROM attend_courses")
    fun selectAsFlow(): Flow<List<AttendCourse>>

    @Query("SELECT * FROM attend_courses WHERE day_of_week = :dayOfWeek ORDER BY period, subject")
    fun selectAsFlow(dayOfWeek: DayOfWeek): Flow<List<AttendCourse>>

    @Query("SELECT * FROM attend_courses WHERE _id = :id")
    fun select(id: Long): AttendCourse?

    @Query("SELECT * FROM attend_courses")
    fun select(): List<AttendCourse>

    @Transaction
    fun insertOrDelete(attendCourseList: List<AttendCourse>) {
        insertIgnore(attendCourseList)

        // Delete old notices
        deleteNotInHash(AttendCourse.Type.PORTAL, attendCourseList.map { it.hash })
    }

    @Insert
    fun insert(attendCourse: AttendCourse): Long

    @Insert
    fun insert(attendCourseList: List<AttendCourse>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnore(attendCourseList: List<AttendCourse>): List<Long>

    @Update
    fun update(attendCourse: AttendCourse): Int

    @Query("DELETE FROM attend_courses WHERE _id = :id")
    fun delete(id: Long): Int

    @Query("DELETE FROM attend_courses WHERE type = :attendType AND subject = :subject")
    fun delete(subject: String, attendType: AttendCourse.Type): Int

    @Query("DELETE FROM attend_courses WHERE type = :attendType AND hash NOT IN (:hash)")
    fun deleteNotInHash(attendType: AttendCourse.Type, hash: List<Long>)
}
