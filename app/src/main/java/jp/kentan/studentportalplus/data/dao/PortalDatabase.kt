package jp.kentan.studentportalplus.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice

@Database(
    entities = [
        Notice::class,
        LectureInformation::class,
        LectureCancellation::class,
        AttendCourse::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(value = [DateConverter::class, AttendCourseConverter::class])
abstract class PortalDatabase : RoomDatabase() {

    abstract val noticeDao: NoticeDao

    abstract val lectureInformationDao: LectureInformationDao

    abstract val lectureCancellationDao: LectureCancellationDao

    abstract val attendCourseDao: AttendCourseDao

}
