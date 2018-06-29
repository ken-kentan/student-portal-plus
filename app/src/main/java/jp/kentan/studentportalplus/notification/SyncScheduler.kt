package jp.kentan.studentportalplus.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import jp.kentan.studentportalplus.util.getSyncIntervalMinutes
import org.jetbrains.anko.defaultSharedPreferences
import java.util.concurrent.TimeUnit


class SyncScheduler {
    companion object {
        private const val TAG = "SyncScheduler"

        fun scheduleIfNeed(context: Context) {
            enqueueUniquePeriodicWork(context, ExistingPeriodicWorkPolicy.KEEP)
        }

        fun schedule(context: Context) {
            enqueueUniquePeriodicWork(context, ExistingPeriodicWorkPolicy.REPLACE)
        }

        private fun enqueueUniquePeriodicWork(context: Context, workPolicy: ExistingPeriodicWorkPolicy) {
            val intervalMinutes = context.defaultSharedPreferences.getSyncIntervalMinutes()

            val workManager = WorkManager.getInstance() ?: let {
                Log.w(TAG, "Failed to get WorkManager instance.")
                return
            }

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(intervalMinutes, TimeUnit.MINUTES, intervalMinutes / 2, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()

            workManager.enqueueUniquePeriodicWork(SyncWorker.NAME, workPolicy, syncWorkRequest)
        }

        fun cancel() {
            val workManager = WorkManager.getInstance() ?: return

            workManager.cancelUniqueWork(SyncWorker.NAME)

            Log.d(TAG, "Sync cancelled")
        }
    }
}