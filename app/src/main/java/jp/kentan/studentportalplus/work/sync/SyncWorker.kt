package jp.kentan.studentportalplus.work.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.data.source.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.notification.NotificationHelper
import jp.kentan.studentportalplus.work.ChildWorkerFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll

class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val attendCourseRepository: AttendCourseRepository,
    private val lectureInfoRepository: LectureInformationRepository,
    private val lectureCancelRepository: LectureCancellationRepository,
    private val noticeRepository: NoticeRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "SyncWorker"

        const val KEY_DATA_MESSAGE = "message"
        const val KEY_DATA_IS_AUTH_ERROR = "is_auth_error"
    }

    override suspend fun doWork(): Result = coroutineScope {
        runCatching {
            // should sync first
            attendCourseRepository.syncWithRemote()

            val lectureInfoJob = async {
                lectureInfoRepository.syncWithRemote()
            }

            val lectureCancelJob = async {
                lectureCancelRepository.syncWithRemote()
            }

            val noticeDeferred = async {
                noticeRepository.syncWithRemote()
            }

            joinAll(lectureInfoJob, lectureCancelJob)

            notificationHelper.sendNotice(noticeDeferred.await())
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
