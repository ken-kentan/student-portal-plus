package jp.kentan.studentportalplus.data

import android.util.Log
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import jp.kentan.studentportalplus.util.formatYearMonthDay
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*

object DocumentParser {

    private const val TAG = "DocumentParser"

    private val ASIA_TOKYO: TimeZone = TimeZone.getTimeZone("Asia/Tokyo")

    private val LECTURE_DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN).apply {
        timeZone = ASIA_TOKYO
    }
    private val NOTICE_DATE_FORMAT = SimpleDateFormat("yyyy.MM.dd", Locale.JAPAN).apply {
        timeZone = ASIA_TOKYO
    }

    private const val LECTURE_INFO_ELEMENT_SIZE = 11
    private const val LECTURE_CANCEL_ELEMENT_SIZE = 9
    private const val NOTICE_ELEMENT_SIZE = 4
    private const val DEFAULT_ATTEND_COURSE_CREDIT = 0

    fun parseLectureInformation(document: Document): List<LectureInformation> {
        val lectureInfoList = document.select("tr[class~=gen_tbl1_(odd|even)]").mapNotNull {
            val tdElements = it.select("td")
            if (tdElements.size < LECTURE_INFO_ELEMENT_SIZE) {
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
                dayOfWeek = week,
                period = period,
                category = category,
                detailText = detailText,
                detailHtml = detailHtml,
                createdDate = LECTURE_DATE_FORMAT.parseNotNull(createdDateStr),
                updatedDate = LECTURE_DATE_FORMAT.parseNotNull(updatedDateStr)
            )
        }

        Log.d(TAG, "Parsed ${lectureInfoList.size} lecture infos")

        return lectureInfoList
    }

    fun parseLectureCancellation(document: Document): List<LectureCancellation> {
        val lectureCancelList = document.select("tr[class~=gen_tbl1_(odd|even)]").mapNotNull {
            val tdElements = it.select("td")
            if (tdElements.size < LECTURE_CANCEL_ELEMENT_SIZE) {
                return@mapNotNull null
            }

            val grade = tdElements[1].text()
            val subject = tdElements[2].text()
            val instructor = tdElements[3].text()
            val cancelDateStr = tdElements[4].text()
            val cancelDate = LECTURE_DATE_FORMAT.parseNotNull(cancelDateStr)
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
                dayOfWeek = week,
                period = period,
                detailText = detailText,
                detailHtml = detailHtml,
                createdDate = LECTURE_DATE_FORMAT.parseNotNull(createdDateStr)
            )
        }

        Log.d(TAG, "Parsed ${lectureCancelList.size} lecture cancels")

        return lectureCancelList
    }

    fun parseNotice(document: Document): List<Notice> {
        val noticeList = document.select("dl").mapNotNull {
            val ddElements = it.select("dd")
            if (ddElements.size < NOTICE_ELEMENT_SIZE) {
                return@mapNotNull null
            }

            val createdDateStr = ddElements[0].text()
            val inCharge = ddElements[1].text()
            val category = ddElements[2].text()

            val noticeElement = ddElements[3]
            val hrefElement = noticeElement.selectFirst("a")

            val title = if (hrefElement != null) {
                hrefElement.text()
            } else {
                noticeElement.html().substringBefore("<br>").trim()
            }

            val infoElement = noticeElement.selectFirst(".notice_info")

            val detailText = infoElement?.text()
            val detailHtml = infoElement?.html()
            val link = hrefElement?.attr("href")

            return@mapNotNull Notice(
                createdDate = NOTICE_DATE_FORMAT.parseNotNull(createdDateStr),
                inCharge = inCharge,
                category = category,
                title = title,
                detailText = detailText,
                detailHtml = detailHtml,
                link = link
            )
        }

        Log.d(TAG, "Parsed ${noticeList.size} notices")

        return noticeList
    }

    fun parseAttendCourse(document: Document): List<AttendCourse> {
        val attendCourseList = mutableListOf<AttendCourse>()

        var dayOfWeekIndex = 0

        // Timetable (Per dayOfWeek)
        document.select("table#enroll_data_tbl tr[class~=gen_tbl1_(odd|even)]").forEach {
            val tdElements = it.select("td")
            if (tdElements.size != 7) {
                return@forEach
            }

            ++dayOfWeekIndex

            // Per period
            tdElements.forEachIndexed { index, element ->
                val pElement = element.selectFirst("p:not([class])") ?: return@forEachIndexed

                val lines = pElement.html().split("<br>", limit = 5)
                if (lines.size < 5) {
                    return@forEachIndexed
                }

                val dayOfWeek = when (dayOfWeekIndex) {
                    1 -> DayOfWeek.MONDAY
                    2 -> DayOfWeek.TUESDAY
                    3 -> DayOfWeek.WEDNESDAY
                    4 -> DayOfWeek.THURSDAY
                    5 -> DayOfWeek.FRIDAY
                    else -> return@forEachIndexed
                }
                val period = index + 1
                val scheduleCode = pElement.selectFirst("a").text()

                attendCourseList.add(
                    createAttendCourse(
                        dayOfWeek,
                        period,
                        scheduleCode,
                        lines
                    )
                )
            }
        }

        // Intensive part
        document.select("table#enroll_data_tbl2 tr td").forEach {
            val lines = it.html().split("<br>", limit = 5)
            if (lines.size < 5) {
                return@forEach
            }

            val scheduleCode = it.selectFirst("a").text()

            attendCourseList.add(
                createAttendCourse(
                    DayOfWeek.INTENSIVE,
                    0,
                    scheduleCode,
                    lines
                )
            )
        }

        Log.d(TAG, "Parsed ${attendCourseList.size} attendType courses")

        return attendCourseList
    }

    private fun createAttendCourse(
        dayOfWeek: DayOfWeek,
        period: Int,
        scheduleCode: String,
        lines: List<String>
    ): AttendCourse {
        val credit = lines[1]
            .filter { it.isDigit() }
            .toIntOrNull() ?: DEFAULT_ATTEND_COURSE_CREDIT

        return AttendCourse(
            dayOfWeek = dayOfWeek,
            period = period,
            scheduleCode = scheduleCode,
            credit = credit,
            category = lines[2].trim(),
            subject = lines[3].trim(),
            instructor = lines[4].trim(),
            type = AttendCourse.Type.PORTAL
        )
    }

    private fun SimpleDateFormat.parseNotNull(source: String): Date = requireNotNull(parse(source))
}
