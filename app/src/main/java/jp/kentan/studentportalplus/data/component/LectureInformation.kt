package jp.kentan.studentportalplus.data.component

import java.util.*


data class LectureInformation(
        val id         : Int = -1,
        val hash       : Int,    // Murmur3.hash32(grade + semester + subject + instructor + week + period + category + detail + createdDate + updatedDate)
        val grade      : String, // 学部名など
        val semester   : String, // 学期
        val subject    : String, // 授業科目名
        val instructor : String, // 担当教員名
        val week       : String, // 曜日
        val period     : String, // 時限
        val category   : String, // 分類
        val detail     : String, // 連絡事項
        val createdDate: Date,   // 初回掲示日
        val updatedDate: Date,   // 最終更新日
        val hasRead    : Boolean = false,
        val attend     : LectureAttendType = LectureAttendType.UNKNOWN
)