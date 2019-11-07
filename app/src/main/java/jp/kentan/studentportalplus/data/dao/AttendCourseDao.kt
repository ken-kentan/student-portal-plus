package jp.kentan.studentportalplus.data.dao

import android.util.Log
import androidx.room.*
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.AttendCourseSubject
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendCourseDao {

    @Query("SELECT * FROM attend_courses WHERE _id = :id")
    fun getFlow(id: Long): Flow<AttendCourse?>

    @Query("SELECT * FROM attend_courses")
    fun getListFlow(): Flow<List<AttendCourse>>

    @Query("SELECT * FROM attend_courses WHERE day_of_week = :dayOfWeek ORDER BY period, subject")
    fun getListFlow(dayOfWeek: DayOfWeek): Flow<List<AttendCourse>>

    @Query("SELECT subject, type FROM attend_courses")
    fun getSubjectListFlow(): Flow<List<AttendCourseSubject>>

    @Query("SELECT * FROM attend_courses WHERE _id = :id")
    fun get(id: Long): AttendCourse?

    @Transaction
    fun updateAll(attendCourseList: List<AttendCourse>): List<AttendCourse> {
        val insertList = mutableListOf<AttendCourse>()

        attendCourseList.forEach { course ->
            val id = insert(course)

            Log.d("AttendCourseDao", "$id: ${course.subject}")
            if (id > 0) {
                insertList.add(course)
            }
        }

        // Delete old notices
        deleteNotInHash(AttendCourse.Type.PORTAL.ordinal, attendCourseList.map { it.hash })

        return insertList
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(attendCourse: AttendCourse): Long

    @Insert
    fun insertAll(attendCourseList: List<AttendCourse>): List<Long>

    @Update
    fun update(attendCourse: AttendCourse): Int

    @Query("DELETE FROM attend_courses WHERE _id = :id")
    fun delete(id: Long): Int

    @Query("DELETE FROM attend_courses WHERE type = :attendType AND subject = :subject")
    fun delete(subject: String, attendType: AttendCourse.Type): Int

    @Query("DELETE FROM attend_courses WHERE type = :type AND hash NOT IN (:hash)")
    fun deleteNotInHash(type: Int, hash: List<Long>)

}
