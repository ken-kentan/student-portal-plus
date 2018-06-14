package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.model.LectureInformation
import jp.kentan.studentportalplus.data.dao.DatabaseOpenHelper
import jp.kentan.studentportalplus.util.Murmur3
import org.jetbrains.anko.db.RowParser
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*


class LectureInformationParser : BaseParser(), RowParser<LectureInformation> {

    private companion object {
        const val TAG = "LectureInfoParser"
        val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    }

    @Throws(Exception::class)
    override fun parse(document: Document): List<LectureInformation> {
        val resultList = mutableListOf<LectureInformation>()

        val trElements = document.select("tr[class~=gen_tbl1_(odd|even)]")

        trElements.forEach{
            val tdElements = it.select("td")
            if (tdElements.size < 11) {
                return@forEach
            }

            val grade          = tdElements[1].text()
            val semester       = tdElements[2].text()
            val subject        = tdElements[3].text()
            val instructor     = tdElements[4].text()
            val week           = tdElements[5].text()
            val period         = tdElements[6].text()
            val category       = tdElements[7].text()
            val detailText     = tdElements[8].text()
            val detailHtml     = tdElements[8].html()
            val createdDateStr = tdElements[9].text()
            val updatedDateStr = tdElements[10].text()

            val hashStr = grade + semester + subject + instructor + week + period + category + detailHtml + createdDateStr + updatedDateStr

            resultList.add(
                    LectureInformation(
                            hash = Murmur3.hash64(hashStr.toByteArray()),
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
            )
        }

        Log.d(TAG, "Parsed ${resultList.size} LectureInformation")

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
            createdDate = DatabaseOpenHelper.toDate(columns[11] as String),
            updatedDate = DatabaseOpenHelper.toDate(columns[12] as String),
            hasRead = (columns[13] as Long) == 1L
    )

    @Throws(Exception::class)
    private fun String.toDate(): Date = try {
        DATE_FORMAT.parse(this)
    } catch (e: Exception) {
        throw ParseException("Failed to parse String($this) to Date")
    }
}