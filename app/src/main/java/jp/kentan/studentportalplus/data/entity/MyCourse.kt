package jp.kentan.studentportalplus.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.kentan.studentportalplus.data.vo.CourseColor
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.util.XxHash64

@Entity(tableName = "my_courses", indices = [Index(value = ["hash"], unique = true)])
data class MyCourse(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: DayOfWeek, // 週

    @ColumnInfo(name = "period")
    val period: Int, // 時限

    @ColumnInfo(name = "schedule_code")
    val scheduleCode: String, // 時間割コード

    @ColumnInfo(name = "credit")
    val credit: Int, // 単位

    @ColumnInfo(name = "category")
    val category: String, // カテゴリ

    @ColumnInfo(name = "subject")
    val subject: String, // 授業科目名

    @ColumnInfo(name = "instructor")
    val instructor: String, // 担当教員名

    @ColumnInfo(name = "is_editable")
    val isEditable: Boolean,

    @ColumnInfo(name = "color")
    val color: CourseColor = CourseColor.DEFAULT,

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "hash")
    val hash: Long = XxHash64.hash("$dayOfWeek$period$scheduleCode$credit$category$subject$instructor$isEditable")
) {
    init {
        require(period in 1..7) { "period($period) should in 1..7" }
    }
}
