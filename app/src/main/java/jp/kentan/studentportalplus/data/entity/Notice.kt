package jp.kentan.studentportalplus.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.kentan.studentportalplus.util.XxHash64
import java.util.Date

@Entity(tableName = "notices", indices = [Index(value = ["hash"], unique = true)])
data class Notice(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "created_date")
    val createdDate: Date, // 掲示日

    @ColumnInfo(name = "in_charge")
    val inCharge: String, // 発信課

    @ColumnInfo(name = "category")
    val category: String, // カテゴリ

    @ColumnInfo(name = "title")
    val title: String, // お知らせ > タイトル

    @ColumnInfo(name = "detail_text")
    val detailText: String?, // お知らせ > 詳細(Text)

    @ColumnInfo(name = "detail_html")
    val detailHtml: String?, // お知らせ > 詳細(Html)

    @ColumnInfo(name = "link")
    val link: String?, // お知らせ > リンク

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "hash")
    val hash: Long = XxHash64.hash("$createdDate$inCharge$category$title$detailHtml$link")
)
