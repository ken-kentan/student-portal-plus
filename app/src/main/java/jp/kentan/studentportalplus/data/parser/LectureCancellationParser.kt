package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.component.LectureCancellationData
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*


class LectureCancellationParser : BaseParser() {

    private companion object {
        const val TAG = "LectureCancelParser"
        val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    }

    @Throws(Exception::class)
    override fun parse(document: Document): List<LectureCancellationData> {
        val resultList = mutableListOf<LectureCancellationData>()

        val trElements = document.select("tr[class~=gen_tbl1_(odd|even)]")

        trElements.forEach{
            val tdElements = it.select("td")
            if (tdElements.size < 10) {
                return@forEach
            }

            resultList.add(
                    LectureCancellationData(
                            grade = tdElements[1].text(),
                            subject = tdElements[2].text(),
                            instructor = tdElements[3].text(),
                            cancelDate = tdElements[4].text().toDate(),
                            week = tdElements[5].text(),
                            period = tdElements[6].text(),
                            category = tdElements[7].text(),
                            detail = tdElements[8].html(),
                            createdDate = tdElements[9].text().toDate()
                    )
            )
        }

        Log.d(TAG, "Parsed ${resultList.size} LectureCancellationData")

        return resultList
    }

    @Throws(Exception::class)
    private fun String.toDate(): Date = try {
        DATE_FORMAT.parse(this)
    } catch (e: Exception) {
        throw ParseException("Failed to parse String($this) to Date")
    }
}