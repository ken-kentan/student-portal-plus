package jp.kentan.studentportalplus.data.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.entity.LectureInformation

class FakeLectureInformationDao : LectureInformationDao {

    companion object {
        const val ALL_LIST_SIZE = 9
    }

    override fun getAsLiveData(id: Long): LiveData<LectureInformation?> =
        MutableLiveData(TestData.lectureInfo)

    override fun getAllAsLiveData(): LiveData<List<LectureInformation>> =
        MutableLiveData(List(ALL_LIST_SIZE) { TestData.lectureInfo })

    override fun updateAsRead(id: Long): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(lectureInfo: LectureInformation): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteNotInHash(hash: List<Long>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
