package jp.kentan.studentportalplus.data.dao

import jp.kentan.studentportalplus.data.entity.LectureInformation
import kotlinx.coroutines.flow.Flow

class FakeLectureInformationDao : LectureInformationDao {

    override fun selectAsFlow(id: Long): Flow<LectureInformation?> {
        TODO("Not yet implemented")
    }

    override fun selectAsFlow(): Flow<List<LectureInformation>> {
        TODO("Not yet implemented")
    }

    override fun updateRead(id: Long): Int {
        TODO("Not yet implemented")
    }

    override fun insert(lectureInfo: LectureInformation): Long {
        TODO("Not yet implemented")
    }

    override fun deleteNotInHash(hash: List<Long>) {
        TODO("Not yet implemented")
    }
}
