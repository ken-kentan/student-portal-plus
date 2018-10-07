package jp.kentan.studentportalplus.data.component

import java.util.*

data class NoticeQuery(
        val keyword: String? = null,
        val dateRange: DateRange = DateRange.ALL,
        val isUnread: Boolean = false,
        val isRead: Boolean = false,
        val isFavorite: Boolean = false
) {

    val keywordList: List<String> by lazy {
        if (keyword == null || keyword.isBlank()) {
            emptyList()
        } else {
            keyword.split(' ')
                    .mapNotNull {
                        val trim = it.trim()
                        if (trim.isNotBlank()) trim else null
                    }
        }
    }

    enum class DateRange(
            private val displayName: String
    ) {
        ALL("全期間"),
        DAY("今日"),
        WEEK("今週"),
        MONTH("今月"),
        YEAR("今年");

        val time: Long
            get() {
                val calendar = Calendar.getInstance().apply {
                    clear(Calendar.MINUTE)
                    clear(Calendar.SECOND)
                    clear(Calendar.MILLISECOND)
                    set(Calendar.HOUR_OF_DAY, 0)
                }

                when(this) {
                    WEEK -> { calendar.set(Calendar.DAY_OF_WEEK , calendar.firstDayOfWeek) }
                    MONTH -> { calendar.set(Calendar.DAY_OF_MONTH, 1) }
                    YEAR -> { calendar.set(Calendar.DAY_OF_YEAR , 1) }
                    else -> {}
                }

                return calendar.timeInMillis
            }

        override fun toString() = displayName
    }
}