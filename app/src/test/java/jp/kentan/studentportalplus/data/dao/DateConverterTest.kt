package jp.kentan.studentportalplus.data.dao

import com.google.common.truth.Truth
import org.junit.Test
import java.sql.Timestamp
import java.util.Date

class DateConverterTest {

    private val converter = DateConverter()

    @Test
    fun `fromTimestamp should return Date if valid Timestamp`() {
        Truth.assertThat(converter.fromTimestamp(123))
            .isEqualTo(Date(123))
    }

    @Test
    fun `fromTimestamp should return null if null Timestamp`() {
        Truth.assertThat(converter.fromTimestamp(null))
            .isNull()
    }

    @Test
    fun `dateToTimestamp should return Timestamp if valid Date`() {
        Truth.assertThat(converter.dateToTimestamp(Date(123)))
            .isEqualTo(Timestamp(123))
    }

    @Test
    fun `dateToTimestamp should return null if null Date`() {
        Truth.assertThat(converter.dateToTimestamp(null))
            .isNull()
    }
}
