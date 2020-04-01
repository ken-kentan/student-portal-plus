package jp.kentan.studentportalplus.work.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import jp.kentan.studentportalplus.data.Preferences
import jp.kentan.studentportalplus.data.source.ShibbolethException
import jp.kentan.studentportalplus.domain.sync.SyncUseCase
import jp.kentan.studentportalplus.notification.NotificationHelper
import jp.kentan.studentportalplus.work.ChildWorkerFactory
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncUseCase: SyncUseCase,
    private val preferences: Preferences,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, params) {

    companion object {
        const val NAME = "sync_work"

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

        fun buildOneTimeWorkRequest() = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setInputData(Data.Builder().putBoolean(KEY_IS_IGNORE_MIDNIGHT, true).build())
            .build()
    }

    override suspend fun doWork(): Result {
        notificationHelper.cancelError()

        if (!inputData.getBoolean(KEY_IS_IGNORE_MIDNIGHT, false) && isInMidnight()) {
            Log.d(TAG, "Skipped work because for midnight")
            return Result.success()
        }

        try {
            val result = syncUseCase()

            notificationHelper.sendLectureInformation(result.updatedLectureInformationList)
            notificationHelper.sendLectureCancellation(result.updatedLectureCancellationList)
            notificationHelper.sendNotice(result.updatedNoticeList)
        } catch (e: Exception) {
            if (e is ShibbolethException) {
                notificationHelper.sendShibbolethError(e.message)
            } else if (preferences.isDetailErrorEnabled) {
                notificationHelper.sendError(e)
            }

            Log.e(TAG, "Failed to sync", e)
            return Result.failure()
        }

        return Result.success()
    }

    private fun isInMidnight(): Boolean {
        val timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        return Calendar.getInstance(timeZone).get(Calendar.HOUR_OF_DAY) !in AVAILABLE_HOUR
    }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory
}
