package jp.kentan.studentportalplus.data.model

import android.support.v7.util.DiffUtil
import jp.kentan.studentportalplus.data.component.LectureAttendType
import java.util.*


data class LectureCancellation(
        val id         : Long = -1,
        val hash       : Long,   // Murmur3.hash64(grade + subject + instructor + cancelDate + week + period + detailHtml + createdDate)
        val grade      : String, // 学部名など
        override val subject    : String, // 授業科目名
        override val instructor : String, // 担当教員名
        val cancelDate : Date,   // 休講日
        override val week       : String, // 曜日
        override val period     : String, // 時限
        val detailText : String, // 概要(Text)
        val detailHtml : String, // 概要(Html)
        val createdDate: Date,   // 初回掲示日
        override val isRead : Boolean = false,
        override val attend : LectureAttendType = LectureAttendType.UNKNOWN
) : Lecture(subject, instructor, week, period, detailText, createdDate, isRead, attend) {
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