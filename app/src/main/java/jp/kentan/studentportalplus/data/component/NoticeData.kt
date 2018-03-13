package jp.kentan.studentportalplus.data.component

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
)