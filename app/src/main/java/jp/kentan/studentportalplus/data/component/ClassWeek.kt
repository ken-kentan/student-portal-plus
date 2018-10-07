package jp.kentan.studentportalplus.data.component

enum class ClassWeek(
        val code: Int,
        val displayName: String
) {
    MONDAY   (1, "月"),
    TUESDAY  (2, "火"),
    WEDNESDAY(3, "水"),
    THURSDAY (4, "木"),
    FRIDAY   (5, "金"),
    SATURDAY (6, "土"),
    SUNDAY   (7, "日"),
    INTENSIVE(8, "集中"),
    UNKNOWN  (0, "-");

    val fullDisplayName = displayName + if (code in 1..7) "曜日" else ""

    override fun toString() = fullDisplayName

    fun hasPeriod() = code in 1..7

    companion object {
        private val ENUMS = values()

        val TIMETABLE = arrayListOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

        fun valueOf(code: Int) =
                ENUMS.find { it.code == code }
                        ?: throw IllegalArgumentException("Invalid ClassWeek code: $code")

        fun valueOfSimilar(name: String) =
                ENUMS.find { name == it.displayName || name == it.fullDisplayName }
                        ?: throw IllegalArgumentException("Invalid ClassWeek name: $name")
    }
}