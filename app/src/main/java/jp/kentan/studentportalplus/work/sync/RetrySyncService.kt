package jp.kentan.studentportalplus.work.sync

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager

class RetrySyncService : IntentService("RetrySyncService") {

    companion object {
        fun createIntent(context: Context) = Intent(context, RetrySyncService::class.java)
    }

    override fun onHandleIntent(intent: Intent?) {
        try {
            val request = SyncWorker.buildOneTimeWorkRequest()
            WorkManager.getInstance(applicationContext)
                .enqueue(request)
        } catch (e: IllegalStateException) {
            Log.e("RetrySyncService", "Failed to schedule SyncWorker", e)
        }
    }
}
