package jp.kentan.studentportalplus.work.sync

import android.content.Context
import androidx.work.*
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import jp.kentan.studentportalplus.data.*
import jp.kentan.studentportalplus.data.entity.AttendCourseSubject
import jp.kentan.studentportalplus.data.entity.Lecture
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.data.entity.calcAttendCourseType
import jp.kentan.studentportalplus.data.source.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.data.vo.LectureNotificationType
import jp.kentan.studentportalplus.data.vo.NoticeNotificationType
import jp.kentan.studentportalplus.notification.NotificationHelper
import jp.kentan.studentportalplus.work.ChildWorkerFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit

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
        const val NAME = "sync_worker"

        const val KEY_DATA_MESSAGE = "message"
        const val KEY_DATA_IS_AUTH_ERROR = "is_auth_error"

        fun buildPeriodicWorkRequest(intervalMinutes: Long): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return PeriodicWorkRequestBuilder<SyncWorker>(
                intervalMinutes,
                TimeUnit.MINUTES,
                intervalMinutes / 2,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        runCatching {
            // should sync first
            attendCourseRepository.syncWithRemote()
            val subjectList = attendCourseRepository.getSubjectList()

            val similarSubjectThreshold = localPreferences.similarSubjectThreshold

            val lectureInfoDeferred = async {
                lectureInfoRepository.syncWithRemote()
                    .filterWith(
                        localPreferences.lectureInformationNotificationType,
                        subjectList,
                        similarSubjectThreshold
                    )
            }

            val lectureCancelDeferred = async {
                lectureCancelRepository.syncWithRemote()
                    .filterWith(
                        localPreferences.lectureCancellationNotificationType,
                        subjectList,
                        similarSubjectThreshold
                    )
            }

            val noticeDeferred = async {
                noticeRepository.syncWithRemote()
                    .filterWith(
                        localPreferences.noticeNotificationType
                    )
            }

            notificationHelper.sendLectureInformation(lectureInfoDeferred.await())
            notificationHelper.sendLectureCancellation(lectureCancelDeferred.await())
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

    private fun <T : Lecture> List<T>.filterWith(
        notificationType: LectureNotificationType,
        subjectList: List<AttendCourseSubject>,
        threshold: Float
    ): List<T> = when (notificationType) {
        LectureNotificationType.ALL -> this
        LectureNotificationType.ATTEND -> filter {
            subjectList.calcAttendCourseType(it.subject, threshold).isAttend
        }
        LectureNotificationType.NOT -> emptyList()
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
