package jp.kentan.studentportalplus.data.component

import android.support.v7.util.DiffUtil
import java.util.*


data class LectureCancellation(
        val id         : Int = -1,
        val hash       : Int,    // Murmur3.hash32(grade + subject + instructor + cancelDate + week + period + detailHtml + createdDate)
        val grade      : String, // 学部名など
        val subject    : String, // 授業科目名
        val instructor : String, // 担当教員名
        val cancelDate : Date,   // 休講日
        val week       : String, // 曜日
        val period     : String, // 時限
        val detailText : String, // 概要(Text)
        val detailHtml : String, // 概要(Html)
        val createdDate: Date,   // 初回掲示日
        val hasRead    : Boolean = false,
        val attend     : LectureAttendType = LectureAttendType.UNKNOWN
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LectureCancellation>() {
            override fun areItemsTheSame(oldItem: LectureCancellation?, newItem: LectureCancellation?): Boolean {
                return oldItem?.id == newItem?.id
            }

            override fun areContentsTheSame(oldItem: LectureCancellation?, newItem: LectureCancellation?): Boolean {
                return  oldItem == newItem
            }
        }
    }
}