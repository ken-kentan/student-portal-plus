package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.entity.LectureInformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLectureInformationDao : LectureInformationDao {

    companion object {
        const val ALL_LIST_SIZE = 9
    }

    override fun getFlow(id: Long): Flow<LectureInformation?> = flowOf(TestData.lectureInfo)

    override fun getListFlow(): Flow<List<LectureInformation>> =
        flowOf(List(ALL_LIST_SIZE) { TestData.lectureInfo })

    override fun updateRead(id: Long): Int {
        check(id == TestData.lectureInfo.id)

        return 1
    }

    override fun insert(lectureInfo: LectureInformation): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteNotInHash(hash: List<Long>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
