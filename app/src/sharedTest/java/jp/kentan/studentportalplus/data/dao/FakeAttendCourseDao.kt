package jp.kentan.studentportalplus.data.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.entity.AttendCourse
import jp.kentan.studentportalplus.data.entity.AttendCourseSubject
import jp.kentan.studentportalplus.data.vo.DayOfWeek

class FakeAttendCourseDao : AttendCourseDao {

    companion object {
        const val ALL_LIST_SIZE = 9
        const val DAY_OF_WEEK_LIST_SIZE = 5
    }

    override fun getAsLiveData(id: Long): LiveData<AttendCourse?> =
        MutableLiveData(TestData.attendCourse)

    override fun getSubjectAsLiveData(): LiveData<List<AttendCourseSubject>> =
        MutableLiveData(listOf(TestData.attendCourseSubject))

    override fun getAllAsLiveData(): LiveData<List<AttendCourse>> =
        MutableLiveData(List(ALL_LIST_SIZE) { TestData.attendCourse })

    override fun getAllAsLiveData(dayOfWeek: DayOfWeek): LiveData<List<AttendCourse>> =
        MutableLiveData(List(DAY_OF_WEEK_LIST_SIZE) { TestData.attendCourse })

    override fun get(id: Long): AttendCourse? = TestData.attendCourse

    override fun insert(attendCourse: AttendCourse): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(attendCourse: AttendCourse): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(id: Long): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteNotInHash(type: Int, hash: List<Long>) {}
}
