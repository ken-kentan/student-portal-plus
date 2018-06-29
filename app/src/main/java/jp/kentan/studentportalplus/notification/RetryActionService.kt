package jp.kentan.studentportalplus.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class RetryActionService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val syncWorkRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                .setInputData(Data.Builder().putBoolean("ignore_midnight", true).build())
                .build()

        WorkManager.getInstance()?.enqueue(syncWorkRequest)

        return START_NOT_STICKY
    }
}
