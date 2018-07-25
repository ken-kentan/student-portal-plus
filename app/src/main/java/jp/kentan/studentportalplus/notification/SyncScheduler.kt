package jp.kentan.studentportalplus.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import jp.kentan.studentportalplus.util.enabledSync
import jp.kentan.studentportalplus.util.getSyncIntervalMinutes
import org.jetbrains.anko.defaultSharedPreferences
import java.util.concurrent.TimeUnit


class SyncScheduler {
    companion object {
        private const val TAG = "SyncScheduler"

        fun scheduleIfNeed(context: Context) {
            if (context.defaultSharedPreferences.enabledSync()) {
                enqueueUniquePeriodicWork(context, ExistingPeriodicWorkPolicy.KEEP)
            }
        }

        fun schedule(context: Context) {
            enqueueUniquePeriodicWork(context, ExistingPeriodicWorkPolicy.REPLACE)
        }

        private fun enqueueUniquePeriodicWork(context: Context, workPolicy: ExistingPeriodicWorkPolicy) {
            val intervalMinutes = context.defaultSharedPreferences.getSyncIntervalMinutes()

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(intervalMinutes, TimeUnit.MINUTES, intervalMinutes / 2, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()

            try {
                val workManager = WorkManager.getInstance()
                workManager.enqueueUniquePeriodicWork(SyncWorker.NAME, workPolicy, syncWorkRequest)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to enqueue SyncWorker", e)
            }
        }

        fun cancel() {
            try {
                val workManager = WorkManager.getInstance()
                workManager.cancelUniqueWork(SyncWorker.NAME)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to cancel SyncWorker", e)
                return
            }

            Log.d(TAG, "Cancelled SyncWorker")
        }
    }
}