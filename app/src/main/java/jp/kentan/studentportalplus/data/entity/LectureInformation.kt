package jp.kentan.studentportalplus.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.kentan.studentportalplus.util.XxHash64
import java.util.Date

@Entity(tableName = "lecture_infos", indices = [Index(value = ["hash"], unique = true)])
data class LectureInformation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "grade")
    val grade: String, // 学部名など

    @ColumnInfo(name = "semester")
    val semester: String, // 学期

    @ColumnInfo(name = "subject")
    override val subject: String, // 授業科目名

    @ColumnInfo(name = "instructor")
    override val instructor: String, // 担当教員名

    @ColumnInfo(name = "day_of_week")
    override val dayOfWeek: String, // 曜日

    @ColumnInfo(name = "period")
    override val period: String, // 時限

    @ColumnInfo(name = "category")
    val category: String, // 分類

    @ColumnInfo(name = "detail_text")
    val detailText: String, // 連絡事項(Text)

    @ColumnInfo(name = "detail_html")
    val detailHtml: String, // 連絡事項(Html)

    @ColumnInfo(name = "created_date")
    val createdDate: Date, // 初回掲示日

    @ColumnInfo(name = "updated_date")
    val updatedDate: Date, // 最終更新日

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @Ignore
    val attendType: AttendCourse.Type = AttendCourse.Type.UNKNOWN,

    @ColumnInfo(name = "hash")
    val hash: Long = XxHash64.hash("$grade$semester$subject$instructor$dayOfWeek$period$category$detailHtml$createdDate$updatedDate")
) : Lecture {
    // Room constructor
    constructor(
        id: Long,
        grade: String,
        semester: String,
        subject: String,
        instructor: String,
        dayOfWeek: String,
        period: String,
        category: String,
        detailText: String,
        detailHtml: String,
        createdDate: Date,
        updatedDate: Date,
        isRead: Boolean,
        hash: Long
    ) : this(
        id,
        grade,
        semester,
        subject,
        instructor,
        dayOfWeek,
        period,
        category,
        detailText,
        detailHtml,
        createdDate,
        updatedDate,
        isRead,
        AttendCourse.Type.UNKNOWN,
        hash
    )
}
