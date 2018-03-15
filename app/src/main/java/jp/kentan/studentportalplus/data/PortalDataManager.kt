package jp.kentan.studentportalplus.data

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import io.reactivex.subjects.BehaviorSubject
import jp.kentan.studentportalplus.data.component.Notice
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.data.dao.LectureInformationDao
import jp.kentan.studentportalplus.data.dao.NoticeDao
import jp.kentan.studentportalplus.data.dao.database
import jp.kentan.studentportalplus.data.parser.LectureCancellationParser
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.data.parser.NoticeParser
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethClient
import org.jetbrains.anko.coroutines.experimental.bg


class PortalDataManager(context: Context) {

    private companion object {
        const val TAG = "PortalDataManager"
    }

    private val shibbolethClient = ShibbolethClient(context)

    private val noticeParser = NoticeParser()
    private val lectureInformationParser = LectureInformationParser()
    private val lectureCancellationParser = LectureCancellationParser()

    private val noticeDao = NoticeDao(context.database)
    private val lectureInformationDao = LectureInformationDao(context.database)

    val noticeLiveData = MutableLiveData<List<Notice>>()

    /**
     * Sync local data with online
     */
    fun sync() = bg {
        try {
            val noticeList = noticeParser.parse(shibbolethClient.fetch(PortalDataType.NOTICE.url))
            val lecInfoList = lectureInformationParser.parse(shibbolethClient.fetch(PortalDataType.LECTURE_INFORMATION.url))
//            val lecCancelList = lectureCancellationParser.parse(shibbolethClient.fetch(PortalDataType.LECTURE_CANCELLATION.url))

            noticeDao.updateAll(noticeList)
            lectureInformationDao.updateAll(lecInfoList)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync", e)
            return@bg Pair(false, e.message)
        }

        noticeLiveData.postValue(noticeDao.getAll())
        lectureInformationDao.getAll()

        return@bg Pair<Boolean, String?>(true, null)
    }

    fun loadAll() = bg {
        noticeLiveData.postValue(noticeDao.getAll())
    }

    /**
     * Update favorite or read of Notice
     */
    fun update(data: Notice) = bg {
        if (noticeDao.update(data) > 0) {
            noticeLiveData.postValue(noticeDao.getAll())
        }
    }
}