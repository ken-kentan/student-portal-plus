package jp.kentan.studentportalplus.work.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import jp.kentan.studentportalplus.data.*
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.data.source.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.data.vo.NoticeNotificationType
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
    private val localPreferences: LocalPreferences,
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

            // TODO: If auto sync enabled
            val noticeNotificationType = localPreferences.noticeNotificationType
            notificationHelper.sendNotice(noticeDeferred.await().filterWith(noticeNotificationType))
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

    private fun List<Notice>.filterWith(notificationType: NoticeNotificationType) =
        when (notificationType) {
            NoticeNotificationType.ALL -> this
            NoticeNotificationType.IMPORTANT -> filter {
                it.title.contains("重要") || it.title.contains("注意")
            }
            NoticeNotificationType.NOT -> emptyList()
        }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory
}
