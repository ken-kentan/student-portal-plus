package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.model.Notice
import jp.kentan.studentportalplus.data.dao.DatabaseOpenHelper
import jp.kentan.studentportalplus.util.Murmur3
import org.jetbrains.anko.db.RowParser
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*


class NoticeParser : BaseParser(), RowParser<Notice> {

    private companion object {
        const val TAG = "NoticeParser"
        val DATE_FORMAT = SimpleDateFormat("yyyy.MM.dd", Locale.JAPAN)
    }

    @Throws(Exception::class)
    override fun parse(document: Document): List<Notice> {
        val resultList = mutableListOf<Notice>()

        val dlElements = document.select("dl")

        dlElements.forEach{
            val ddElements = it.select("dd")
            if (ddElements.size < 4) {
                return@forEach
            }

            val createdDateStr = ddElements[0].text()
            val inCharge = ddElements[1].text()
            val category = ddElements[2].text()

            val noticeElement = ddElements[3]
            val hrefElement   = noticeElement.selectFirst("a")

            val title = if (hrefElement != null) {
                hrefElement.text()
            } else {
                noticeElement.html().substringBefore("<br>").trim()
            }

            val infoElement = noticeElement.selectFirst(".notice_info")

            val detailText = infoElement?.text()
            val detailHtml = infoElement?.html()
            val link = hrefElement?.attr("href")

            val hashStr = createdDateStr + inCharge + category + title + detailHtml + link

            resultList.add(
                    Notice(
                            hash = Murmur3.hash64(hashStr.toByteArray()),
                            createdDate = createdDateStr.toDate(),
                            inCharge = inCharge,
                            category = category,
                            title = title,
                            detailText = detailText,
                            detailHtml = detailHtml,
                            link = link
                    )
            )
        }

        Log.d(TAG, "Parsed ${resultList.size} Notice")

        return resultList
    }

    override fun parseRow(columns: Array<Any?>) = Notice(
            id = columns[0] as Long,
            hash = columns[1] as Long,
            createdDate = DatabaseOpenHelper.toDate(columns[2] as String),
            inCharge = columns[3] as String,
            category = columns[4] as String,
            title = columns[5] as String,
            detailText = columns[6] as String?,
            detailHtml = columns[7] as String?,
            link = columns[8] as String?,
            hasRead = (columns[9] as Long) == 1L,
            isFavorite = (columns[10] as Long) == 1L
    )

    @Throws(Exception::class)
    private fun String.toDate(): Date = try {
        DATE_FORMAT.parse(this)
    } catch (e: Exception) {
        throw ParseException("Failed to parse String($this) to Date")
    }
}