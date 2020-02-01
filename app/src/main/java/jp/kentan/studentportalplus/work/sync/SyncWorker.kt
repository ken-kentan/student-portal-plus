package jp.kentan.studentportalplus.work.sync

import android.content.Context
import android.util.Log
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
import java.util.*
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

        private const val TAG = "SyncWorker"

        private val AVAILABLE_HOUR = 5..22
        private const val KEY_IS_IGNORE_MIDNIGHT = "is_ignore_midnight"

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

    override suspend fun doWork(): Result {
        if (!inputData.getBoolean(KEY_IS_IGNORE_MIDNIGHT, false) && isInMidnight()) {
            Log.d(TAG, "Skipped work because for midnight")
            return Result.success()
        }

        return coroutineScope {
            try {
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

                Result.success()
            } catch (e: Exception) {
                if (e is ShibbolethAuthenticationException) {
                    notificationHelper.sendAuthenticationError(e.message)
                } else if (localPreferences.isEnabledDetailError) {
                    notificationHelper.sendError(e)
                }

                Log.e(TAG, "Failed to sync", e)
                Result.failure()
            }
        }
    }

    private fun isInMidnight(): Boolean {
        val timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        return Calendar.getInstance(timeZone).get(Calendar.HOUR_OF_DAY) !in AVAILABLE_HOUR
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
