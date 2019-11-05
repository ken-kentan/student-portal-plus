package jp.kentan.studentportalplus.work.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import jp.kentan.studentportalplus.data.dao.PortalDatabase
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.work.ChildWorkerFactory

class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val database: PortalDatabase,
    private val shibbolethClient: ShibbolethClient
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "SyncWorker"

        private const val ATTEND_COURSE_URL =
            "https://portal.student.kit.ac.jp/ead/?c=attend_course"
        private const val LECTURE_INFO_URL =
            "https://portal.student.kit.ac.jp/ead/?c=lecture_information"
        private const val LECTURE_CANCEL_URL =
            "https://portal.student.kit.ac.jp/ead/?c=lecture_cancellation"
        private const val NOTICE_URL = "https://portal.student.kit.ac.jp"
    }

    override suspend fun doWork(): Result {
        runCatching {
            val attendCourseDocument = shibbolethClient.fetch(ATTEND_COURSE_URL)
            val lectureInfoDocument = shibbolethClient.fetch(LECTURE_INFO_URL)
            val lectureCancelDocument = shibbolethClient.fetch(LECTURE_CANCEL_URL)
            val noticeDocument = shibbolethClient.fetch(NOTICE_URL)

            val attendCourseList = DocumentParser.parseAttendCourse(attendCourseDocument)
            val lectureInfoList = DocumentParser.parseLectureInformation(lectureInfoDocument)
            val lectureCancelList = DocumentParser.parseLectureCancellation(lectureCancelDocument)
            val noticeList = DocumentParser.parseNotice(noticeDocument)

            database.attendCourseDao.updateAll(attendCourseList)
            database.lectureInformationDao.updateAll(lectureInfoList)
            database.lectureCancellationDao.updateAll(lectureCancelList)
            database.noticeDao.updateAll(noticeList)
        }.fold(
            onSuccess = {
                return Result.success()
            },
            onFailure = {
                return Result.failure()
            }
        )
    }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory
}