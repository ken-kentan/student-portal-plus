package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.component.LectureInformationData
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*


class LectureInformationParser : BaseParser() {

    private companion object {
        const val TAG = "LectureInfoParser"
        val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    }

    @Throws(Exception::class)
    override fun parse(document: Document): List<LectureInformationData> {
        val resultList = mutableListOf<LectureInformationData>()

        val trElements = document.select("tr[class~=gen_tbl1_(odd|even)]")

        trElements.forEach{
            val tdElements = it.select("td")
            if (tdElements.size < 11) {
                return@forEach
            }

            resultList.add(
                    LectureInformationData(
                            grade = tdElements[1].text(),
                            semester = tdElements[2].text(),
                            subject = tdElements[3].text(),
                            instructor = tdElements[4].text(),
                            week = tdElements[5].text(),
                            period = tdElements[6].text(),
                            category = tdElements[7].text(),
                            detail = tdElements[8].html(),
                            createdDate = tdElements[9].text().toDate(),
                            updatedDate = tdElements[10].text().toDate()
                    )
            )
        }

        Log.d(TAG, "Parsed ${resultList.size} LectureInformationData")

        return resultList
    }

    @Throws(Exception::class)
    private fun String.toDate(): Date = try {
        DATE_FORMAT.parse(this)
    } catch (e: Exception) {
        throw ParseException("Failed to parse String($this) to Date")
    }
}