package jp.kentan.studentportalplus.data.component


/**
 * Class week type
 *
 * https://portal.student.kit.ac.jp/ead/?c=attend_course
 */
enum class ClassWeekType(val code: Int, val displayName: String) {
    MONDAY   (1, "月"),
    TUESDAY  (2, "火"),
    WEDNESDAY(3, "水"),
    THURSDAY (4, "木"),
    FRIDAY   (5, "金"),
    SATURDAY (6, "土"),
    SUNDAY   (7, "日"),
    INTENSIVE(8, "集中等"),
    UNKNOWN  (9, "-");

    val fullDisplayName: String

    init {
        fullDisplayName = displayName + if (code in 1..7) "曜日" else ""
    }

    companion object {
        private val ENUMS = values()

        fun valueOf(code: Int): ClassWeekType {
            if (code in 1..9) {
                return ENUMS[code-1]
            }

            throw Exception("Invalid ClassWeekType code: $code")
        }

        fun valueOfSimilar(name: String): ClassWeekType {
            ENUMS.forEach {
                if (name == it.displayName || name == it.fullDisplayName) {
                    return it
                }
            }

            throw Exception("Invalid ClassWeekType name: $name")
        }
    }
}