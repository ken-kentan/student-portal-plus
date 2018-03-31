package jp.kentan.studentportalplus.data.model

import jp.kentan.studentportalplus.data.component.LectureAttendType

abstract class Lecture(
        open val subject   : String,
        open val instructor: String,
        open val week      : String,
        open val period    : String,
        open val attend    : LectureAttendType
)