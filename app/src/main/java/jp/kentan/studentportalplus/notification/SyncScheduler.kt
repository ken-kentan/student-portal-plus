package jp.kentan.studentportalplus.notification

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import jp.kentan.studentportalplus.util.getSyncIntervalMinutes
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast
import java.util.concurrent.TimeUnit

class SyncScheduler {
    companion object {
        private const val TAG = "SyncScheduler"

        private val workManager by lazy { WorkManager.getInstance() }

        fun scheduleIfNeed(context: Context) {
            workManager.getStatusesByTag(SyncWorker.TAG).observeForever {
                val result = it ?: return@observeForever

                // Debug code
                val sb = StringBuilder()
                result.forEach {
                    Log.d(TAG, "${it.id}: ${it.state.name}")
                    sb.append("${it.id}: ${it.state.name}\n")
                }
                context.longToast(sb.toString())

                if (result.filterNot { it.state.isFinished }.isEmpty()) {
                    schedule(context)
                }
            }
        }

        fun schedule(context: Context) {
            val intervalMinutes = context.defaultSharedPreferences.getSyncIntervalMinutes()

            val syncWork = PeriodicWorkRequest.Builder(
                    SyncWorker::class.java,
                    intervalMinutes, TimeUnit.MINUTES,
                    intervalMinutes - 5L, TimeUnit.MINUTES)
                    .addTag(SyncWorker.TAG)
                    .setConstraints(createConstraints())
                    .build()

            cancel()

            workManager.enqueue(syncWork)

            Log.d(TAG, "Scheduled the SyncWorker(interval: ${intervalMinutes}minutes)")
        }

        fun cancel() {
            workManager.cancelAllWorkByTag(SyncWorker.TAG)
        }

        private fun createConstraints(): Constraints {
            return Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
        }
    }
}