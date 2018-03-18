package jp.kentan.studentportalplus.data.parser

import jp.kentan.studentportalplus.data.component.LectureAttendType
import org.jetbrains.anko.db.RowParser


class LectureAttendParser : RowParser<Pair<String, LectureAttendType>> {

    override fun parseRow(columns: Array<Any?>): Pair<String, LectureAttendType> {
        return Pair(columns[0] as String, LectureAttendType.valueOf(columns[1] as String))
    }

}