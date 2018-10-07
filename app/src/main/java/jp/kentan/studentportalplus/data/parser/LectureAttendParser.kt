package jp.kentan.studentportalplus.data.parser

import jp.kentan.studentportalplus.data.component.LectureAttend
import org.jetbrains.anko.db.RowParser


class LectureAttendParser : RowParser<Pair<String, LectureAttend>> {

    override fun parseRow(columns: Array<Any?>): Pair<String, LectureAttend> {
        return Pair(columns[0] as String, if ((columns[1] as Long) == 1L) LectureAttend.USER else LectureAttend.PORTAL)
    }

}