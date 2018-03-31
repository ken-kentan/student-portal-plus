package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.component.ClassWeekType
import jp.kentan.studentportalplus.data.component.LectureAttendType
import jp.kentan.studentportalplus.data.model.Lecture
import jp.kentan.studentportalplus.data.model.MyClass
import jp.kentan.studentportalplus.util.Murmur3
import jp.kentan.studentportalplus.util.toIntOrNull
import org.jetbrains.anko.db.RowParser
import org.jsoup.nodes.Document


class MyClassParser : BaseParser(), RowParser<MyClass> {

    private companion object {
        const val TAG = "MyClassParser"
    }

    @Throws(Exception::class)
    override fun parse(document: Document): List<MyClass> {
        val resultList = mutableListOf<MyClass>()

        val trElements = document.select("table#enroll_data_tbl tr[class~=gen_tbl1_(odd|even)]")

        var weekCode = 0

        // Timetable (Per week)
        trElements.forEach{
            val tdElements = it.select("td")
            if (tdElements.size != 7) {
                throw ParseException("Unknown attend_course layout")
            }

            ++weekCode

            // Per period
            tdElements.forEachIndexed { index, element ->
                val pElement = element.selectFirst("p:not(.semester_title3)") ?: return@forEachIndexed

                val lineList = pElement.html().split("<br>", limit = 5)

                if (lineList.size < 5) {
                    return@forEachIndexed
                }

                val week         = ClassWeekType.valueOf(weekCode)
                val period       = index + 1
                val scheduleCode = pElement.selectFirst("a").text()

                resultList.add(toMyClass(week, period, scheduleCode, lineList))
            }
        }

        // Intensive part
        val td2Elements = document.select("table#enroll_data_tbl2 tr td")

        td2Elements.forEach {
            val lineList = it.html().split("<br>", limit = 5)

            if (lineList.size < 5) {
                return@forEach
            }

            val scheduleCode = it.selectFirst("a").text()

            resultList.add(toMyClass(ClassWeekType.INTENSIVE, 0, scheduleCode, lineList))
        }

        Log.d(TAG, "Parsed ${resultList.size} MyClass")

        return resultList
    }

    override fun parseRow(columns: Array<Any?>) = MyClass(
            id           = columns[0] as Long,
            hash         = columns[1] as Long,
            week         = ClassWeekType.valueOf((columns[2] as Long).toInt()),
            period       = (columns[3] as Long).toInt(),
            scheduleCode = columns[4] as String,
            credit       = (columns[5] as Long).toInt(),
            category     = columns[6] as String,
            subject      = columns[7] as String,
            instructor   = columns[8] as String,
            isUser       = (columns[9] as Long) == 1L,
            location     = columns[10] as String?
    )

    @Throws(Exception::class)
    fun parse(data: Lecture): List<MyClass> {
        val list = mutableListOf<MyClass>()

        val week = ClassWeekType.valueOfSimilar(data.week)
        val periodList = mutableListOf<Int>()
        if (data.period.length >= 3) {
            val first = data.period[0]
            val last  = data.period[data.period.lastIndex]

            val periodFirst = first.toIntOrNull() ?: throw ParseException("Invalid period: $first")
            val periodLast  = last.toIntOrNull()  ?: throw ParseException("Invalid period: $last")

            if (periodFirst > periodLast) {
                throw ParseException("Invalid period range: $first to $last")
            }

            for (p in periodFirst..periodLast) {
                periodList.add(p)
            }
        } else {
            val period = data.period.toIntOrNull() ?: 0
            periodList.add(period)
        }

        if (data.attend != LectureAttendType.PORTAL && data.attend != LectureAttendType.USER) {
            throw ParseException("Invalid lecture attend type: ${data.attend.name}")
        }

        periodList.forEach {
            val hashStr = week.name + it + 0 + data.subject + data.instructor

            list.add(
                    MyClass(
                            hash         = Murmur3.hash64(hashStr.toByteArray()),
                            week         = week,
                            period       = it,
                            scheduleCode = "",
                            credit       = 0,
                            category     = "",
                            subject      = data.subject,
                            instructor   = data.instructor,
                            isUser       = (data.attend == LectureAttendType.USER)
                    )
            )
        }

        return list
    }

    private fun toMyClass(week: ClassWeekType, period: Int, scheduleCode: String, lineList: List<String>): MyClass {
        val credit       = lineList[1].filter { it.isDigit() }.toIntOrNull() ?: throw ParseException("Invalid credit: ${lineList[1]}")
        val category     = lineList[2].trim()
        val subject      = lineList[3].trim()
        val instructor   = lineList[4].trim()

        val hashStr = week.name + period + scheduleCode + credit + category + subject + instructor

        return MyClass(
                hash = Murmur3.hash64(hashStr.toByteArray()),
                week = week,
                period = period,
                scheduleCode = scheduleCode,
                credit = credit,
                category = category,
                subject = subject,
                instructor = instructor,
                isUser = false
        )
    }
}