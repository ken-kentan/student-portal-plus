package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.model.LectureInformation
import org.jetbrains.anko.db.RowParser
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*


class LectureInformationParser : BaseParser(), RowParser<LectureInformation> {

    private companion object {
        val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    }

    @Throws(Exception::class)
    fun parse(document: Document): List<LectureInformation> {
        val resultList = document.select("tr[class~=gen_tbl1_(odd|even)]").mapNotNull {
            val tdElements = it.select("td")
            if (tdElements.size < 11) {
                return@mapNotNull null
            }

            val grade = tdElements[1].text()
            val semester = tdElements[2].text()
            val subject = tdElements[3].text()
            val instructor = tdElements[4].text()
            val week = tdElements[5].text()
            val period = tdElements[6].text()
            val category = tdElements[7].text()
            val detailText = tdElements[8].text()
            val detailHtml = tdElements[8].html()
            val createdDateStr = tdElements[9].text()
            val updatedDateStr = tdElements[10].text()

            return@mapNotNull LectureInformation(
                    grade = grade,
                    semester = semester,
                    subject = subject,
                    instructor = instructor,
                    week = week,
                    period = period,
                    category = category,
                    detailText = detailText,
                    detailHtml = detailHtml,
                    createdDate = createdDateStr.toDate(),
                    updatedDate = updatedDateStr.toDate()
            )
        }

        Log.d("LectureInfoParser", "Parsed ${resultList.size} LectureInformation")

        return resultList
    }

    override fun parseRow(columns: Array<Any?>) = LectureInformation(
            id = columns[0] as Long,
            hash = columns[1] as Long,
            grade = columns[2] as String,
            semester = columns[3] as String,
            subject = columns[4] as String,
            instructor = columns[5] as String,
            week = columns[6] as String,
            period = columns[7] as String,
            category = columns[8] as String,
            detailText = columns[9] as String,
            detailHtml = columns[10] as String,
            createdDate = Date(columns[11] as Long),
            updatedDate = Date(columns[12] as Long),
            isRead = (columns[13] as Long) == 1L
    )

    @Throws(Exception::class)
    private fun String.toDate(): Date = try {
        DATE_FORMAT.parse(this)
    } catch (e: Exception) {
        throw ParseException("Failed to parse String($this) to Date")
    }
}