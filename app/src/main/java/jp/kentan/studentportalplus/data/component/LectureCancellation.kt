package jp.kentan.studentportalplus.data.component

import java.util.*


data class LectureCancellation(
        val id         : Int = -1,
        val hash       : Int,
        val grade      : String, // 学部名など
        val subject    : String, // 授業科目名
        val instructor : String, // 担当教員名
        val cancelDate : Date,   // 休講日
        val week       : String, // 曜日
        val period     : String, // 時限
        val detail     : String, // 概要
        val createdDate: Date,   // 初回掲示日
        val hasRead    : Boolean = false,
        val attend     : LectureAttendType = LectureAttendType.UNKNOWN
)