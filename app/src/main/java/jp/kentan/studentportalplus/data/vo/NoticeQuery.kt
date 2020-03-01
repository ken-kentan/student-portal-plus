package jp.kentan.studentportalplus.data.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import jp.kentan.studentportalplus.R
import java.util.*

data class NoticeQuery(
    val text: String? = null,
    val dateRange: DateRange = DateRange.ALL,
    val isUnread: Boolean = false,
    val isRead: Boolean = false,
    val isFavorite: Boolean = false
) : Parcelable {

    enum class DateRange(
        @StringRes val resId: Int
    ) {
        ALL(R.string.date_range_all),
        DAY(R.string.date_range_day),
        WEEK(R.string.date_range_week),
        MONTH(R.string.date_range_month),
        YEAR(R.string.date_range_year);

        fun createTimeInMillis(calender: Calendar) = calender.apply {
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
            set(Calendar.HOUR_OF_DAY, 0)

            when (this@DateRange) {
                ALL -> clear()
                DAY -> return@apply
                WEEK -> set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                MONTH -> set(Calendar.DAY_OF_MONTH, 1)
                YEAR -> set(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis
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
        parcel.readInt() == 1,
        parcel.readInt() == 1,
        parcel.readInt() == 1
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeSerializable(dateRange)
        parcel.writeInt(if (isUnread) 1 else 0)
        parcel.writeInt(if (isRead) 1 else 0)
        parcel.writeInt(if (isFavorite) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<NoticeQuery> {
        override fun createFromParcel(parcel: Parcel) = NoticeQuery(parcel)
        override fun newArray(size: Int): Array<NoticeQuery?> = arrayOfNulls(size)
    }
}
