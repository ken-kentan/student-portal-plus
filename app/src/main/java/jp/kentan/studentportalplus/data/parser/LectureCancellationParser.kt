package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.component.LectureCancellationData
import jp.kentan.studentportalplus.util.Murmur3
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
            if (tdElements.size < 9) {
                return@forEach
            }

            val grade          = tdElements[1].text()
            val subject        = tdElements[2].text()
            val instructor     = tdElements[3].text()
            val cancelDateStr  = tdElements[4].text()
            val week           = tdElements[5].text()
            val period         = tdElements[6].text()
            val detail         = tdElements[7].html()
            val createdDateStr = tdElements[8].text()

            val hashStr = grade + subject + instructor + cancelDateStr + week + period + detail + createdDateStr

            resultList.add(
                    LectureCancellationData(
                            hash        = Murmur3.hash32(hashStr.toByteArray()),
                            grade       = grade,
                            subject     = subject,
                            instructor  = instructor,
                            cancelDate  = cancelDateStr.toDate(),
                            week        = week,
                            period      = period,
                            detail      = detail,
                            createdDate = createdDateStr.toDate()
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