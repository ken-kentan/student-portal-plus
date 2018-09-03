package jp.kentan.studentportalplus.data.model

import jp.kentan.studentportalplus.data.component.LectureAttendType
import java.util.Date

abstract class Lecture(
        open val subject   : String,
        open val instructor: String,
        open val week      : String,
        open val period    : String,
        open val detail    : String,
        open val date      : Date,
        open val isRead    : Boolean,
        open val attend    : LectureAttendType
)