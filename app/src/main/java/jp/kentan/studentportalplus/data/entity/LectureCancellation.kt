package jp.kentan.studentportalplus.data.entity

import androidx.room.*
import jp.kentan.studentportalplus.util.Murmur3
import java.util.*

@Entity(tableName = "lecture_cancels", indices = [Index(value = ["hash"], unique = true)])
data class LectureCancellation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "grade")
    val grade: String, // 学部名など

    @ColumnInfo(name = "subject")
    override val subject: String, // 授業科目名

    @ColumnInfo(name = "instructor")
    override val instructor: String, // 担当教員名

    @ColumnInfo(name = "cancel_date")
    val cancelDate: Date, // 休講日

    @ColumnInfo(name = "dayOfWeek")
    override val dayOfWeek: String, // 曜日

    @ColumnInfo(name = "period")
    override val period: String, // 時限

    @ColumnInfo(name = "detail_text")
    val detailText: String, // 概要(Text)

    @ColumnInfo(name = "detail_html")
    val detailHtml: String, // 概要(Html)

    @ColumnInfo(name = "created_date")
    val createdDate: Date, // 初回掲示日

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @Ignore
    val attendType: AttendCourse.Type = AttendCourse.Type.UNKNOWN,

    @ColumnInfo(name = "hash")
    val hash: Long = Murmur3.hash64("$grade$subject$instructor$cancelDate$dayOfWeek$period$detailHtml$createdDate")
) : Lecture {
    // Room constructor
    constructor(
        id: Long,
        grade: String,
        subject: String,
        instructor: String,
        cancelDate: Date,
        dayOfWeek: String,
        period: String,
        detailText: String,
        detailHtml: String,
        createdDate: Date,
        isRead: Boolean,
        hash: Long
    ) : this(
        id,
        grade,
        subject,
        instructor,
        cancelDate,
        dayOfWeek,
        period,
        detailText,
        detailHtml,
        createdDate,
        isRead,
        AttendCourse.Type.UNKNOWN,
        hash
    )
}