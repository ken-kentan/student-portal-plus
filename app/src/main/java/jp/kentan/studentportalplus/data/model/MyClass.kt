package jp.kentan.studentportalplus.data.model

import androidx.recyclerview.widget.DiffUtil
import jp.kentan.studentportalplus.data.component.ClassColor
import jp.kentan.studentportalplus.data.component.ClassWeek
import jp.kentan.studentportalplus.util.Murmur3


data class MyClass(
        val id: Long = -1,
        val week: ClassWeek, // 週
        val period: Int, // 時限
        val scheduleCode: String, // 時間割コード
        val credit: Int, // 単位
        val category: String, // カテゴリ
        val subject: String, // 授業科目名
        val instructor: String, // 担当教員名
        val isUser: Boolean, // true: LectureAttend.USER, false: LectureAttend.PORTAL
        val color: Int = ClassColor.DEFAULT,
        val location: String? = null,
        val hash: Long = Murmur3.hash64("$week$period$scheduleCode$credit$category$subject$instructor$isUser")
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MyClass>() {
            override fun areItemsTheSame(oldItem: MyClass, newItem: MyClass): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MyClass, newItem: MyClass): Boolean {
                return oldItem == newItem
            }
        }
    }
}