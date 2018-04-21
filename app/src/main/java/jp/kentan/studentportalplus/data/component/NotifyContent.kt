package jp.kentan.studentportalplus.data.component

data class NotifyContent(
        val title: String,
        val text : String?,
        val id   : Long // for Database
)