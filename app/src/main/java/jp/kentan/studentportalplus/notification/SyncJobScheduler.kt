package jp.kentan.studentportalplus.notification

import android.content.Context
import android.util.Log
import com.firebase.jobdispatcher.*
import org.jetbrains.anko.defaultSharedPreferences

class SyncJobScheduler {

    companion object {

        private const val TAG = "SyncJobScheduler"

        fun scheduleIfNeed(context: Context) {
            if (!context.defaultSharedPreferences.getBoolean("enable_sync", true)) {
                return
            }

            val lastSyncTimeMillis = context.defaultSharedPreferences.getLong("last_sync_time_millis", 0)
            val diffHours = (System.currentTimeMillis() - lastSyncTimeMillis) / (1000 * 60 * 60)

            if (diffHours > 12) {
                schedule(context)
            }
        }

        fun schedule(context: Context) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
            val intervalSec = (context.defaultSharedPreferences.getString("sync_interval", "60").toIntOrNull() ?: 60) * 60

            try {
                val job = dispatcher.newJobBuilder()
                        .setService(SyncJobService::class.java)
                        .setTag(SyncJobService.TAG)
                        .setRecurring(true)
                        .setLifetime(Lifetime.FOREVER)
                        .setTrigger(Trigger.executionWindow(intervalSec, intervalSec + 60))
                        .setReplaceCurrent(true)
                        .setConstraints(Constraint.ON_ANY_NETWORK)
                        .build()

                dispatcher.mustSchedule(job)
            } catch (e: Exception) {
                Log.e(TAG, "failed to schedule SyncJobService", e)
            }

            Log.d(TAG, "scheduled SyncJobService")
        }

        fun cancel(context: Context) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
            dispatcher.cancelAll()
        }
    }
}