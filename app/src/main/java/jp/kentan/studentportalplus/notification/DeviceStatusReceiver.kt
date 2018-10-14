package jp.kentan.studentportalplus.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DeviceStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            SyncScheduler(context).scheduleIfNeeded()
        }
    }
}