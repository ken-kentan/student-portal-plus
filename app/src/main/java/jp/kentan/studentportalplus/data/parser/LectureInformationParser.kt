package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.component.LectureInformationData
import jp.kentan.studentportalplus.util.Murmur3
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

            val grade          = tdElements[1].text()
            val semester       = tdElements[2].text()
            val subject        = tdElements[3].text()
            val instructor     = tdElements[4].text()
            val week           = tdElements[5].text()
            val period         = tdElements[6].text()
            val category       = tdElements[7].text()
            val detail         = tdElements[8].html()
            val createdDateStr = tdElements[9].text()
            val updatedDateStr = tdElements[10].text()

            val hashStr = grade + semester + subject + instructor + week + period + category + detail + createdDateStr + updatedDateStr

            resultList.add(
                    LectureInformationData(
                            hash        = Murmur3.hash32(hashStr.toByteArray()),
                            grade       = grade,
                            semester    = semester,
                            subject     = subject,
                            instructor  = instructor,
                            week        = week,
                            period      = period,
                            category    = category,
                            detail      = detail,
                            createdDate = createdDateStr.toDate(),
                            updatedDate = updatedDateStr.toDate()
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