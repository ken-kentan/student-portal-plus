package jp.kentan.studentportalplus.data

import android.content.Context
import android.util.Log
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.data.dao.NoticeDao
import jp.kentan.studentportalplus.data.parser.LectureCancellationParser
import jp.kentan.studentportalplus.data.parser.LectureInformationParser
import jp.kentan.studentportalplus.data.parser.NoticeParser
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethClient
import org.jetbrains.anko.coroutines.experimental.bg


class PortalDataManager(val context: Context) {

    private companion object {
        const val TAG = "PortalDataManager"
    }

    private val shibbolethClient = ShibbolethClient(context)

    private val noticeParser = NoticeParser()
    private val lectureInformationParser = LectureInformationParser()
    private val lectureCancellationParser = LectureCancellationParser()

    private val noticeDao = NoticeDao(context)

    /**
     * Sync local data with online
     */
    fun sync() = bg {
        try {
            val noticeList = noticeParser.parse(shibbolethClient.fetch(PortalDataType.NOTICE.url))
            val lecInfoList = lectureInformationParser.parse(shibbolethClient.fetch(PortalDataType.LECTURE_INFORMATION.url))
            val lecCancelList = lectureCancellationParser.parse(shibbolethClient.fetch(PortalDataType.LECTURE_CANCELLATION.url))

            noticeDao.updateAll(noticeList)
//            noticeDao.updateAll(lecInfoList)
//            noticeDao.updateAll(lecCancelList)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync", e)
            return@bg Pair(false, e.message)
        }

        return@bg Pair<Boolean, String?>(true, null)
    }
}