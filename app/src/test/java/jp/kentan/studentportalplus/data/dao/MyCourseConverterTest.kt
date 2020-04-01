package jp.kentan.studentportalplus.data.dao

import com.google.common.truth.Truth
import jp.kentan.studentportalplus.data.vo.CourseColor
import jp.kentan.studentportalplus.data.vo.DayOfWeek
import org.junit.Test

class MyCourseConverterTest {

    private val converter = MyCourseConverter()

    @Test
    fun `fromDayOfWeek should return name if valid DayOfWeek`() {
        Truth.assertThat(converter.fromDayOfWeek(DayOfWeek.SUNDAY.name))
            .isEqualTo(DayOfWeek.SUNDAY)
    }

    @Test
    fun `fromDayOfWeek should return null if null DayOfWeek`() {
        Truth.assertThat(converter.fromDayOfWeek(null))
            .isNull()
    }

    @Test
    fun `dayOfWeekTo should return name if valid DayOfWeek`() {
        Truth.assertThat(converter.dayOfWeekTo(DayOfWeek.SUNDAY))
            .isEqualTo(DayOfWeek.SUNDAY.name)
    }

    @Test
    fun `dayOfWeekTo should return null if null DayOfWeek`() {
        Truth.assertThat(converter.dayOfWeekTo(null))
            .isNull()
    }

    @Test
    fun `fromColor should return CourseColor if valid name`() {
        Truth.assertThat(converter.fromColor(CourseColor.LIGHT_BLUE_1.name))
            .isEqualTo(CourseColor.LIGHT_BLUE_1)
    }

    @Test
    fun `fromColor should return null if null name`() {
        Truth.assertThat(converter.fromColor(null))
            .isNull()
    }

    @Test
    fun `colorTo should return name if valid CourseColor`() {
        Truth.assertThat(converter.colorTo(CourseColor.LIGHT_BLUE_1))
            .isEqualTo(CourseColor.LIGHT_BLUE_1.name)
    }

    @Test
    fun `colorTo should return null if null CourseColor`() {
        Truth.assertThat(converter.colorTo(null))
            .isNull()
    }
}
