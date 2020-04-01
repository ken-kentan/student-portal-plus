package jp.kentan.studentportalplus.data.dao

import androidx.room.TypeConverter
import jp.kentan.studentportalplus.data.vo.CourseColor
import jp.kentan.studentportalplus.data.vo.DayOfWeek

class MyCourseConverter {

    @TypeConverter
    fun fromDayOfWeek(name: String?) = name?.let(DayOfWeek::valueOf)

    @TypeConverter
    fun dayOfWeekTo(dayOfWeek: DayOfWeek?) = dayOfWeek?.name

    @TypeConverter
    fun fromColor(name: String?) = name?.let(CourseColor::valueOf)

    @TypeConverter
    fun colorTo(color: CourseColor?) = color?.name
}
