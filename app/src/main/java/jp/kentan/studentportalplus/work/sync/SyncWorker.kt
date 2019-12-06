package jp.kentan.studentportalplus.work.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import jp.kentan.studentportalplus.data.dao.PortalDatabase
import jp.kentan.studentportalplus.data.source.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.work.ChildWorkerFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll

class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val database: PortalDatabase,
    private val shibbolethClient: ShibbolethClient
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "SyncWorker"

        const val KEY_DATA_MESSAGE = "message"
        const val KEY_DATA_IS_AUTH_ERROR = "is_auth_error"

        private const val ATTEND_COURSE_URL =
            "https://portal.student.kit.ac.jp/ead/?c=attend_course"
        private const val LECTURE_INFO_URL =
            "https://portal.student.kit.ac.jp/ead/?c=lecture_information"
        private const val LECTURE_CANCEL_URL =
            "https://portal.student.kit.ac.jp/ead/?c=lecture_cancellation"
        private const val NOTICE_URL = "https://portal.student.kit.ac.jp"
    }

    override suspend fun doWork(): Result = coroutineScope {
        runCatching {
            // should login first
            val attendCourseDocument = shibbolethClient.fetch(ATTEND_COURSE_URL)

            val attendCourseJob = async {
                val attendCourseList = DocumentParser.parseAttendCourse(attendCourseDocument)
                database.attendCourseDao.updateAll(attendCourseList)
            }

            val lectureInfoJob = async {
                val lectureInfoDocument = shibbolethClient.fetch(LECTURE_INFO_URL)
                val lectureInfoList =
                    DocumentParser.parseLectureInformation(lectureInfoDocument)
                database.lectureInformationDao.updateAll(lectureInfoList)
            }

            val lectureCancelJob = async {
                val lectureCancelDocument = shibbolethClient.fetch(LECTURE_CANCEL_URL)
                val lectureCancelList =
                    DocumentParser.parseLectureCancellation(lectureCancelDocument)
                database.lectureCancellationDao.updateAll(lectureCancelList)
            }

            val noticeJob = async {
                val noticeDocument = shibbolethClient.fetch(NOTICE_URL)
                val noticeList = DocumentParser.parseNotice(noticeDocument)
                database.noticeDao.updateAll(noticeList)
            }

            joinAll(attendCourseJob, lectureInfoJob, lectureCancelJob, noticeJob)
        }.fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                val outputData = Data.Builder()
                    .putString(KEY_DATA_MESSAGE, it.message)
                    .putBoolean(KEY_DATA_IS_AUTH_ERROR, it is ShibbolethAuthenticationException)
                    .build()

                Result.failure(outputData)
            }
        )
    }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory
}
