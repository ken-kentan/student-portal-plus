package jp.kentan.studentportalplus.data.entity

import androidx.annotation.ColorRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.util.Murmur3

@Entity(tableName = "attend_courses", indices = [Index(value = ["hash"], unique = true)])
data class AttendCourse(
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

    @ColumnInfo(name = "type")
    val type: Type,

    @ColumnInfo(name = "color")
    val color: Color = Color.DEFAULT,

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "hash")
    val hash: Long = Murmur3.hash64("$dayOfWeek$period$scheduleCode$credit$category$subject$instructor$type")
) {
    enum class Type {
        PORTAL,  // ポータル取得
        USER,    // ユーザー登録
        SIMILAR, // 類似
        NOT,     // 未受講
        UNKNOWN; // 未確認

        val isAttend: Boolean
            get() = this == PORTAL || this == USER || this == SIMILAR

        val canAttend: Boolean
            get() = this == SIMILAR || this == NOT
    }

    enum class Color(
        @ColorRes val resId: Int
    ) {
        LIGHT_BLUE_1(R.color.course_light_blue_1),
        LIGHT_BLUE_2(R.color.course_light_blue_1),
        LIGHT_BLUE_3(R.color.course_light_blue_1),
        LIGHT_BLUE_4(R.color.course_light_blue_1);

        companion object {
            val DEFAULT = LIGHT_BLUE_1
        }
    }

    init {
        require(type == Type.PORTAL || type == Type.USER) { "type($type) should be PORTAL or USER" }
    }
}