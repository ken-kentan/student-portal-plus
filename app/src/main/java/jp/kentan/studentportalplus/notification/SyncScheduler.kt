package jp.kentan.studentportalplus.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import jp.kentan.studentportalplus.util.getSyncIntervalMinutes
import jp.kentan.studentportalplus.util.isEnabledSync
import org.jetbrains.anko.defaultSharedPreferences
import java.util.concurrent.TimeUnit

class SyncScheduler(
        private val context: Context
) {

    companion object {
        private const val TAG = "SyncScheduler"
    }

    fun scheduleIfNeeded() {
        if (context.defaultSharedPreferences.isEnabledSync()) {
            enqueueUniquePeriodicWork(ExistingPeriodicWorkPolicy.KEEP)
        }
    }

    fun schedule() {
        enqueueUniquePeriodicWork(ExistingPeriodicWorkPolicy.REPLACE)
    }

    fun cancel() {
        try {
            WorkManager.getInstance()
                    .cancelUniqueWork(SyncWorker.NAME)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Failed to cancel SyncWorker", e)
            return
        }

        Log.d(TAG, "Cancelled a unique SyncWorker")
    }

    private fun enqueueUniquePeriodicWork(workPolicy: ExistingPeriodicWorkPolicy) {
        val intervalMinutes = context.defaultSharedPreferences.getSyncIntervalMinutes()

        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(intervalMinutes, TimeUnit.MINUTES, intervalMinutes / 2, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        try {
            WorkManager.getInstance()
                    .enqueueUniquePeriodicWork(SyncWorker.NAME, workPolicy, syncWorkRequest)

            Log.d(TAG, "Enqueued a unique SyncWorker")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Failed to enqueue SyncWorker", e)
        }
    }
}