package jp.kentan.studentportalplus.data.component


enum class CreatedDateType(private val string: String){
    ALL("全期間"),
    DAY("今日"),
    WEEK("今週"),
    MONTH("今月"),
    YEAR("今年");

    override fun toString() = string
}
