package jp.kentan.studentportalplus.data.model

import androidx.recyclerview.widget.DiffUtil
import jp.kentan.studentportalplus.util.Murmur3
import java.util.*

data class Notice(
        val id: Long = -1,
        val createdDate: Date, // 掲示日
        val inCharge: String, // 発信課
        val category: String, // カテゴリ
        val title: String, // お知らせ > タイトル
        val detailText: String?, // お知らせ > 詳細(Text)
        val detailHtml: String?, // お知らせ > 詳細(Html)
        val link: String?, // お知らせ > リンク
        val isRead: Boolean = false,
        val isFavorite: Boolean = false,
        val hash: Long = Murmur3.hash64("$createdDate$inCharge$category$title$detailHtml$link")
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Notice>() {
            override fun areItemsTheSame(oldItem: Notice, newItem: Notice): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Notice, newItem: Notice): Boolean {
                return oldItem == newItem
            }
        }
    }
}