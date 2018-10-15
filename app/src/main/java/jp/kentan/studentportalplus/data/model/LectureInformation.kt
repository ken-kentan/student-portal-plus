package jp.kentan.studentportalplus.data.model

import androidx.recyclerview.widget.DiffUtil
import jp.kentan.studentportalplus.data.component.LectureAttend
import jp.kentan.studentportalplus.util.Murmur3
import java.util.*

data class LectureInformation(
        override val id: Long = -1,
        val grade: String, // 学部名など
        val semester: String, // 学期
        override val subject: String, // 授業科目名
        override val instructor: String, // 担当教員名
        override val week: String, // 曜日
        override val period: String, // 時限
        val category: String, // 分類
        val detailText: String, // 連絡事項(Text)
        val detailHtml: String, // 連絡事項(Html)
        val createdDate: Date, // 初回掲示日
        val updatedDate: Date, // 最終更新日
        override val isRead: Boolean = false,
        override val attend: LectureAttend = LectureAttend.UNKNOWN,
        val hash: Long = Murmur3.hash64("$grade$semester$subject$instructor$week$period$category$detailHtml$createdDate$updatedDate")
) : Lecture(detailText, updatedDate) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LectureInformation>() {
            override fun areItemsTheSame(oldItem: LectureInformation, newItem: LectureInformation): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LectureInformation, newItem: LectureInformation): Boolean {
                return oldItem == newItem
            }
        }
    }
}