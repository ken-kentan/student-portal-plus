package jp.kentan.studentportalplus.data.component

import android.support.v7.util.DiffUtil
import java.util.*


data class LectureInformation(
        val id         : Int = -1,
        val hash       : Int,    // Murmur3.hash32(grade + semester + subject + instructor + week + period + category + detailHtml + createdDate + updatedDate)
        val grade      : String, // 学部名など
        val semester   : String, // 学期
        val subject    : String, // 授業科目名
        val instructor : String, // 担当教員名
        val week       : String, // 曜日
        val period     : String, // 時限
        val category   : String, // 分類
        val detailText : String, // 連絡事項(Text)
        val detailHtml : String, // 連絡事項(Html)
        val createdDate: Date,   // 初回掲示日
        val updatedDate: Date,   // 最終更新日
        val hasRead    : Boolean = false,
        val attend     : LectureAttendType = LectureAttendType.UNKNOWN
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LectureInformation>() {
            override fun areItemsTheSame(oldItem: LectureInformation?, newItem: LectureInformation?): Boolean {
                return oldItem?.id == newItem?.id
            }

            override fun areContentsTheSame(oldItem: LectureInformation?, newItem: LectureInformation?): Boolean {
                return  oldItem == newItem
            }
        }
    }
}