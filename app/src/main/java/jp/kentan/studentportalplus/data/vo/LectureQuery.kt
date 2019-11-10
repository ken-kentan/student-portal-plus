package jp.kentan.studentportalplus.data.vo

import android.os.Parcel
import android.os.Parcelable

data class LectureQuery(
    val text: String? = null,
    val isUnread: Boolean = false,
    val isRead: Boolean = false,
    val isAttend: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    val textList: List<String> = if (text.isNullOrBlank()) {
        emptyList()
    } else {
        text.split(' ', '　')
            .mapNotNull {
                val trim = it.trim()
                if (trim.isNotBlank()) trim else null
            }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeByte(if (isUnread) 1 else 0)
        parcel.writeByte(if (isRead) 1 else 0)
        parcel.writeByte(if (isAttend) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<LectureQuery> {
        override fun createFromParcel(parcel: Parcel) = LectureQuery(parcel)
        override fun newArray(size: Int): Array<LectureQuery?> = arrayOfNulls(size)
    }
}
