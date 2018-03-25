package jp.kentan.studentportalplus.data.component

import android.support.v7.util.DiffUtil
import java.util.*


data class Notice(
        val id         : Long = -1,
        val hash       : Long,    // Murmur3.hash64(createdDateStr + inCharge + category + title + detailHtml + link)
        val createdDate: Date,    // 掲示日
        val inCharge   : String,  // 発信課
        val category   : String,  // カテゴリ
        val title      : String,  // お知らせ > タイトル
        val detailText : String?, // お知らせ > 詳細(Text)
        val detailHtml : String?, // お知らせ > 詳細(Html)
        val link       : String?, // お知らせ > リンク
        val hasRead    : Boolean = false,
        val isFavorite : Boolean = false
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Notice>() {
            override fun areItemsTheSame(oldItem: Notice?, newItem: Notice?): Boolean {
                return oldItem?.id == newItem?.id
            }

            override fun areContentsTheSame(oldItem: Notice?, newItem: Notice?): Boolean {
                return  oldItem == newItem
            }
        }
    }
}