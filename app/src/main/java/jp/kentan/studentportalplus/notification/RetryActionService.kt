package jp.kentan.studentportalplus.notification

import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

class RetryActionService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val syncWork = OneTimeWorkRequest.Builder(SyncWorker::class.java)
//                .setInputData(Data.Builder().putBoolean("ignore_midnight", true).build())
//                .build()
//
//        WorkManager.getInstance().enqueue(syncWork)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            val syncJob = JobInfo.Builder(2, ComponentName(this, SyncJobService::class.java))
                    .build()

            jobScheduler.schedule(syncJob)
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
