package jp.kentan.studentportalplus.data.parser

import android.util.Log
import jp.kentan.studentportalplus.data.component.ClassWeek
import jp.kentan.studentportalplus.data.component.LectureAttend
import jp.kentan.studentportalplus.data.model.Lecture
import jp.kentan.studentportalplus.data.model.MyClass
import org.jetbrains.anko.db.RowParser
import org.jsoup.nodes.Document

class MyClassParser : BaseParser(), RowParser<MyClass> {

    @Throws(Exception::class)
    fun parse(document: Document): List<MyClass> {
        val resultList = mutableListOf<MyClass>()

        var weekCode = 0

        // Timetable (Per week)
        document.select("table#enroll_data_tbl tr[class~=gen_tbl1_(odd|even)]").forEach {
            val tdElements = it.select("td")
            if (tdElements.size != 7) {
                throw ParseException("Unknown attend_course layout")
            }

            ++weekCode

            // Per period
            tdElements.forEachIndexed { index, element ->
                val pElement = element.selectFirst("p:not([class])") ?: return@forEachIndexed

                val lineList = pElement.html().split("<br>", limit = 5)

                if (lineList.size < 5) {
                    return@forEachIndexed
                }

                val week = ClassWeek.valueOf(weekCode)
                val period = index + 1
                val scheduleCode = pElement.selectFirst("a").text()

                resultList.add(createMyClass(week, period, scheduleCode, lineList))
            }
        }

        // Intensive part
        document.select("table#enroll_data_tbl2 tr td").forEach {
            val lineList = it.html().split("<br>", limit = 5)

            if (lineList.size < 5) {
                return@forEach
            }

            val scheduleCode = it.selectFirst("a").text()

            resultList.add(createMyClass(ClassWeek.INTENSIVE, 0, scheduleCode, lineList))
        }

        Log.d("MyClassParser", "Parsed ${resultList.size} MyClass")

        return resultList
    }

    @Throws(Exception::class)
    fun parse(data: Lecture): List<MyClass> {
        val week = ClassWeek.valueOfSimilar(data.week)
        val periodList = mutableListOf<Int>()

        if (data.period.length >= 3) {
            val first = data.period.find { it.isDigit() }
                    ?: throw ParseException("First period not found.")
            val last = data.period.findLast { it.isDigit() }
                    ?: throw ParseException("Last period not found.")

            val periodFirst = first.toString().toInt()
            val periodLast = last.toString().toInt()

            if (periodFirst > periodLast) {
                throw ParseException("Invalid period range: $first to $last")
            }

            for (p in periodFirst..periodLast) {
                periodList.add(p)
            }
        } else {
            val period = data.period.find { it.isDigit() }?.toString()?.toInt() ?: 0
            periodList.add(period)
        }

        if (data.attend != LectureAttend.PORTAL && data.attend != LectureAttend.USER) {
            throw ParseException("Not supported LectureAttend: ${data.attend.name}")
        }

        return periodList.map { period ->
            MyClass(
                    week = week,
                    period = period,
                    scheduleCode = "",
                    credit = 0,
                    category = "",
                    subject = data.subject,
                    instructor = data.instructor,
                    isUser = data.attend == LectureAttend.USER
            )
        }
    }

    override fun parseRow(columns: Array<Any?>) = MyClass(
            id = columns[0] as Long,
            hash = columns[1] as Long,
            week = ClassWeek.valueOf((columns[2] as Long).toInt()),
            period = (columns[3] as Long).toInt(),
            scheduleCode = columns[4] as String,
            credit = (columns[5] as Long).toInt(),
            category = columns[6] as String,
            subject = columns[7] as String,
            instructor = columns[8] as String,
            isUser = (columns[9] as Long) == 1L,
            color = (columns[10] as Long).toInt(),
            location = columns[11] as String?
    )

    private fun createMyClass(week: ClassWeek, period: Int, scheduleCode: String, lineList: List<String>): MyClass {
        val credit = lineList[1]
                .filter { it.isDigit() }
                .toIntOrNull()
                ?: throw ParseException("Invalid credit: ${lineList[1]}")

        return MyClass(
                week = week,
                period = period,
                scheduleCode = scheduleCode,
                credit = credit,
                category = lineList[2].trim(),
                subject = lineList[3].trim(),
                instructor = lineList[4].trim(),
                isUser = false
        )
    }
}