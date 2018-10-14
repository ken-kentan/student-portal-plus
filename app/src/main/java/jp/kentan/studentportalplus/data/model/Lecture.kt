package jp.kentan.studentportalplus.data.model

import jp.kentan.studentportalplus.data.component.LectureAttend
import java.util.*

abstract class Lecture(
        open val detail: String,
        open val date: Date
) {
    abstract val id: Long
    abstract val subject: String
    abstract val instructor: String
    abstract val week: String
    abstract val period: String
    abstract val isRead: Boolean
    abstract val attend: LectureAttend
}