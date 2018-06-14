package jp.kentan.studentportalplus.data.component

import jp.kentan.studentportalplus.data.dao.escapeQuery

data class NoticeQuery(
        val keywords  : String?,
        val type      : CreatedDateType,
        val isUnread  : Boolean,
        val hasRead   : Boolean,
        val isFavorite: Boolean
) {
    val keywordList: List<String> by lazy {
        if (keywords == null || keywords.isEmpty()) {
            emptyList()
        } else {
            keywords.split(' ')
                    .mapNotNull {
                        val trim = it.trim()
                        if (trim.isNotEmpty()) trim.escapeQuery() else null
                    }
        }
    }

    companion object {
        val DEFAULT = NoticeQuery(null, CreatedDateType.ALL, true, true, true)
    }
}

fun NoticeQuery.isDefault() = (this == NoticeQuery.DEFAULT)