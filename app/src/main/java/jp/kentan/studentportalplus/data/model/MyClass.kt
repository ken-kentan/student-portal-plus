package jp.kentan.studentportalplus.data.model

import android.graphics.Color
import android.support.v7.util.DiffUtil
import jp.kentan.studentportalplus.data.component.ClassWeekType


data class MyClass(
        val id          : Long = -1,
        val hash        : Long,          // Murmur3.hash64(week + period + scheduleCode + credit + category + subject + instructor)
        val week        : ClassWeekType, // 週
        val period      : Int,           // 時限
        val scheduleCode: String,        // 時間割コード
        val credit      : Int,           // 単位
        val category    : String,        // カテゴリ
        val subject     : String,        // 授業科目名
        val instructor  : String,        // 担当教員名
        val isUser      : Boolean,       // true: LectureAttendType.USER, false: LectureAttendType.PORTAL
        val color       : Int = Color.parseColor("#4FC3F7"), // Light Blue 300
        val location    : String? = null
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MyClass>() {
            override fun areItemsTheSame(oldItem: MyClass?, newItem: MyClass?): Boolean {
                return oldItem?.id == newItem?.id
            }

            override fun areContentsTheSame(oldItem: MyClass?, newItem: MyClass?): Boolean {
                return  oldItem == newItem
            }
        }
    }

    fun match(period: Int, week: ClassWeekType) = (period == this.period) && (week == this.week)
}