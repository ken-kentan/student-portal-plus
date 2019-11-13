package jp.kentan.studentportalplus.data.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import jp.kentan.studentportalplus.R

data class NoticeQuery(
    val text: String? = null,
    val dateRange: DateRange = DateRange.ALL,
    val isUnread: Boolean = false,
    val isRead: Boolean = false,
    val isAttend: Boolean = false
) : Parcelable {

    enum class DateRange(
        @StringRes val resId: Int
    ) {
        ALL(R.string.name_date_range_all),
        DAY(R.string.name_date_range_day),
        WEEK(R.string.name_date_range_week),
        MONTH(R.string.name_date_range_month),
        YEAR(R.string.name_date_range_year)
    }

    val textList: List<String> = if (text.isNullOrBlank()) {
        emptyList()
    } else {
        text.split(' ', '　')
            .mapNotNull {
                val trim = it.trim()
                if (trim.isNotBlank()) trim else null
            }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readSerializable() as DateRange,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeSerializable(dateRange)
        parcel.writeByte(if (isUnread) 1 else 0)
        parcel.writeByte(if (isRead) 1 else 0)
        parcel.writeByte(if (isAttend) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<NoticeQuery> {
        override fun createFromParcel(parcel: Parcel) = NoticeQuery(parcel)
        override fun newArray(size: Int): Array<NoticeQuery?> = arrayOfNulls(size)
    }
}
