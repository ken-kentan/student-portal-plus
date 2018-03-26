package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.model.LectureCancellation
import jp.kentan.studentportalplus.data.dao.DatabaseOpenHelper
import jp.kentan.studentportalplus.util.Murmur3
import jp.kentan.studentportalplus.util.toShortString
import org.jetbrains.anko.db.RowParser
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*


class LectureCancellationParser : BaseParser(), RowParser<LectureCancellation> {

    private companion object {
        const val TAG = "LectureCancelParser"
        val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    }

    @Throws(Exception::class)
    override fun parse(document: Document): List<LectureCancellation> {
        val resultList = mutableListOf<LectureCancellation>()

        val trElements = document.select("tr[class~=gen_tbl1_(odd|even)]")

        trElements.forEach{
            val tdElements = it.select("td")
            if (tdElements.size < 9) {
                return@forEach
            }

            val grade          = tdElements[1].text()
            val subject        = tdElements[2].text()
            val instructor     = tdElements[3].text()
            val cancelDateStr  = tdElements[4].text()
            val cancelDate     = cancelDateStr.toDate()
            val week           = tdElements[5].text()
            val period         = tdElements[6].text()
            val detailText     = cancelDate.toShortString() + ' ' +  tdElements[7].text()
            val detailHtml     = tdElements[7].html()
            val createdDateStr = tdElements[8].text()

            val hashStr = grade + subject + instructor + cancelDateStr + week + period + detailHtml + createdDateStr

            resultList.add(
                    LectureCancellation(
                            hash = Murmur3.hash64(hashStr.toByteArray()),
                            grade = grade,
                            subject = subject,
                            instructor = instructor,
                            cancelDate = cancelDate,
                            week = week,
                            period = period,
                            detailText = detailText,
                            detailHtml = detailHtml,
                            createdDate = createdDateStr.toDate()
                    )
            )
        }

        Log.d(TAG, "Parsed ${resultList.size} LectureCancellationData")

        return resultList
    }

    override fun parseRow(columns: Array<Any?>) = LectureCancellation(
            id = columns[0] as Long,
            hash = columns[1] as Long,
            grade = columns[2] as String,
            subject = columns[3] as String,
            instructor = columns[4] as String,
            cancelDate = DatabaseOpenHelper.toDate(columns[5] as String),
            week = columns[6] as String,
            period = columns[7] as String,
            detailText = columns[8] as String,
            detailHtml = columns[9] as String,
            createdDate = DatabaseOpenHelper.toDate(columns[10] as String),
            hasRead = (columns[11] as Long) == 1L
    )

    @Throws(Exception::class)
    private fun String.toDate(): Date = try {
        DATE_FORMAT.parse(this)
    } catch (e: Exception) {
        throw ParseException("Failed to parse String($this) to Date")
    }
}