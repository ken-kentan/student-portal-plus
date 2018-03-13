package jp.kentan.studentportalplus.data.component

import android.support.v7.util.DiffUtil
import java.util.*


data class NoticeData(
        val id          :Int = -1,
        val hash        :Int,     // Murmur3.hash32(createdDateStr + inCharge + category + title + detail + link)
        val createdDate :Date,    // 掲示日
        val inCharge    :String,  // 発信課
        val category    :String,  // カテゴリ
        val title       :String,  // お知らせ > タイトル
        val detail      :String?, // お知らせ > 詳細
        val link        :String?, // お知らせ > リンク
        val hasRead     :Boolean = false,
        val isFavorite  :Boolean = false
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NoticeData>() {
            override fun areItemsTheSame(oldItem: NoticeData?, newItem: NoticeData?): Boolean {
                return oldItem?.id == newItem?.id
            }

            override fun areContentsTheSame(oldItem: NoticeData?, newItem: NoticeData?): Boolean {
                return  oldItem == newItem
            }
        }
    }
}