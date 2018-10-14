package jp.kentan.studentportalplus.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class RetryActionService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val syncWorkRequest = OneTimeWorkRequest.Builder(SyncWorker::class.java)
                .setInputData(Data.Builder().putBoolean(SyncWorker.IGNORE_MIDNIGHT, true).build())
                .build()

        try {
            WorkManager.getInstance()
                    .enqueue(syncWorkRequest)
        } catch (e: IllegalStateException) {
            Log.e("RetryActionService", "Failed to enqueue a SyncWorker", e)
        }

        return START_NOT_STICKY
    }
}
