package jp.kentan.studentportalplus.data.vo

data class LectureQuery(
    val text: String? = null,
    val isUnread: Boolean = false,
    val isRead: Boolean = false,
    val isAttend: Boolean = false
) {
    val textList: List<String> = if (text.isNullOrBlank()) {
        emptyList()
    } else {
        text.split(' ', '　')
            .mapNotNull {
                val trim = it.trim()
                if (trim.isNotBlank()) trim else null
            }
    }
}
