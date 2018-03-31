package jp.kentan.studentportalplus.data.component

import jp.kentan.studentportalplus.data.dao.escapeQuery

data class LectureQuery(
        val keywords: String?,
        val order   : LectureOrderType,
        val isUnread: Boolean,
        val hasRead : Boolean,
        val isAttend: Boolean
) {
    val keywordList by lazy {
        if (keywords == null || keywords.isEmpty()) {
            listOf<String>()
        } else {
            keywords.split(' ')
                    .mapNotNull {
                        val trim = it.trim()
                        if (trim.isNotEmpty()) trim.escapeQuery() else null
                    }
        }
    }

    companion object {
        val DEFAULT = LectureQuery(null, LectureOrderType.UPDATED_DATE, true, true, true)
    }
}

fun LectureQuery.isDefault() = (this == LectureQuery.DEFAULT)