package jp.kentan.studentportalplus.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.jetbrains.anko.defaultSharedPreferences

class DeviceStatusReceiver : BroadcastReceiver() {

    private companion object {
        const val TAG = "DeviceStatusReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED &&
                context.defaultSharedPreferences.getBoolean("enable_sync", true)) {
            Log.d(TAG, "onReceive: ${intent.action}")
            SyncJobScheduler.schedule(context)
        }
    }
}