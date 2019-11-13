package jp.kentan.studentportalplus.data.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import jp.kentan.studentportalplus.R

data class LectureQuery(
    val text: String? = null,
    val order: Order = Order.UPDATED_DATE,
    val isUnread: Boolean = false,
    val isRead: Boolean = false,
    val isAttend: Boolean = false
) : Parcelable {

    enum class Order(
        @StringRes val resId: Int
    ) {
        UPDATED_DATE(R.string.name_order_update_date),
        ATTEND_CLASS(R.string.name_order_attend_course)
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
        parcel.readSerializable() as Order,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeSerializable(order)
        parcel.writeByte(if (isUnread) 1 else 0)
        parcel.writeByte(if (isRead) 1 else 0)
        parcel.writeByte(if (isAttend) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<LectureQuery> {
        override fun createFromParcel(parcel: Parcel): LectureQuery {
            return LectureQuery(parcel)
        }

        override fun newArray(size: Int): Array<LectureQuery?> {
            return arrayOfNulls(size)
        }
    }
}
