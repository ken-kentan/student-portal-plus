package jp.kentan.studentportalplus.data.dao

import androidx.room.TypeConverter
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.vo.DayOfWeek

class AttendCourseConverter {

    @TypeConverter
    fun fromType(name: String?) = name?.let(AttendCourse.Type::valueOf)

    @TypeConverter
    fun typeTo(type: AttendCourse.Type?) = type?.name

    @TypeConverter
    fun fromDayOfWeek(name: String?) = name?.let(DayOfWeek::valueOf)

    @TypeConverter
    fun dayOfWeekTo(dayOfWeek: DayOfWeek?) = dayOfWeek?.name

    @TypeConverter
    fun fromColor(name: String?) = name?.let(AttendCourse.Color::valueOf)

    @TypeConverter
    fun colorTo(color: AttendCourse.Color?) = color?.name

}