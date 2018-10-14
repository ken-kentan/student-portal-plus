package jp.kentan.studentportalplus.data.component


data class LectureQuery(
        val keyword: String? = null,
        val order: Order = Order.UPDATED_DATE,
        val isUnread: Boolean = false,
        val isRead: Boolean = false,
        val isAttend: Boolean = false
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

    enum class Order(
            private val displayName: String
    ) {
        UPDATED_DATE("最終更新日"),
        ATTEND_CLASS("受講科目優先");

        override fun toString() = displayName
    }
}