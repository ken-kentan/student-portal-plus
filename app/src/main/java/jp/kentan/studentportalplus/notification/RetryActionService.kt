package jp.kentan.studentportalplus.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder

class RetryActionService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val syncWork = OneTimeWorkRequest.Builder(SyncWorker::class.java)
//                .setInputData(Data.Builder().putBoolean("ignore_midnight", true).build())
//                .build()
//
//        WorkManager.getInstance().enqueue(syncWork)

        return super.onStartCommand(intent, flags, startId)
    }
}
