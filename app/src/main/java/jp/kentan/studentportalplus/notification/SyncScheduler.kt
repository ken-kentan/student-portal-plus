package jp.kentan.studentportalplus.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.support.annotation.RequiresApi
import android.util.Log
import androidx.core.content.edit
import jp.kentan.studentportalplus.util.getSyncIntervalMinutes
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.intentFor
import java.util.concurrent.TimeUnit


class SyncScheduler {
    companion object {
        private const val TAG = "SyncScheduler"

        fun scheduleIfNeed(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.getJobScheduler().allPendingJobs.find { it.id == SyncJobService.ID } ?: schedule(context)
            } else {

            }
        }

        fun schedule(context: Context) {
            val intervalMillis = TimeUnit.MINUTES.toMillis(context.defaultSharedPreferences.getSyncIntervalMinutes())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val scheduler = context.getJobScheduler()
                scheduler.cancelAll()

                context.saveScheduledTime(System.currentTimeMillis())

                val syncJob = JobInfo.Builder(SyncJobService.ID, ComponentName(context, SyncJobService::class.java))
                        .setPeriodic(intervalMillis)
                        .setPersisted(true)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .build()

                if (scheduler.schedule(syncJob) == JobScheduler.RESULT_SUCCESS) {
                    Log.d(TAG, "Scheduled the SyncJobService")
                } else {
                    Log.e(TAG, "Failed to schedule SyncJobService")
                    context.saveScheduledTime(0)
                }
            } else {
                val triggerAtMillis = SystemClock.elapsedRealtime() + intervalMillis

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, intervalMillis, context.createSyncService())
            }
        }

        fun cancel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                scheduler.cancelAll()
            } else {
                val syncService = context.createSyncService()

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(syncService)
                syncService.cancel()
            }

            Log.d(TAG, "Sync cancelled")
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun Context.getJobScheduler() = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        private fun Context.createSyncService(): PendingIntent {
            return PendingIntent.getService(this, 0, intentFor<SyncService>(), PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun Context.saveScheduledTime(timeMillis: Long) {
            defaultSharedPreferences.edit {
                putLong("scheduled_time_millis", timeMillis)
            }
        }
    }
}