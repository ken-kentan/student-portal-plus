package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.util.formatYearMonthDay
import org.jetbrains.anko.db.RowParser
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*

class LectureCancellationParser : BaseParser(), RowParser<LectureCancellation> {

    @Throws(Exception::class)
    fun parse(document: Document): List<LectureCancellation> {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)

        val resultList = document.select("tr[class~=gen_tbl1_(odd|even)]").mapNotNull {
            val tdElements = it.select("td")
            if (tdElements.size < 9) {
                return@mapNotNull null
            }

            val grade = tdElements[1].text()
            val subject = tdElements[2].text()
            val instructor = tdElements[3].text()
            val cancelDateStr = tdElements[4].text()
            val cancelDate = dateFormat.parse(cancelDateStr)
            val week = tdElements[5].text()
            val period = tdElements[6].text()
            val detailText = cancelDate.formatYearMonthDay() + " — " + instructor
            val detailHtml = tdElements[7].html()
            val createdDateStr = tdElements[8].text()

            return@mapNotNull LectureCancellation(
                    grade = grade,
                    subject = subject,
                    instructor = instructor,
                    cancelDate = cancelDate,
                    week = week,
                    period = period,
                    detailText = detailText,
                    detailHtml = detailHtml,
                    createdDate = dateFormat.parse(createdDateStr)
            )
        }

        Log.d("LectureCancelParser", "Parsed ${resultList.size} LectureCancellation")

        return resultList
    }

    override fun parseRow(columns: Array<Any?>) = LectureCancellation(
            id = columns[0] as Long,
            hash = columns[1] as Long,
            grade = columns[2] as String,
            subject = columns[3] as String,
            instructor = columns[4] as String,
            cancelDate = Date(columns[5] as Long),
            week = columns[6] as String,
            period = columns[7] as String,
            detailText = columns[8] as String,
            detailHtml = columns[9] as String,
            createdDate = Date(columns[10] as Long),
            isRead = (columns[11] as Long) == 1L
    )
}