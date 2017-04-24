package jp.kentan.student_portal_plus.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;


public class DeviceStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences pref = context.getSharedPreferences("common", MODE_PRIVATE);

        if (pref.getBoolean("auto_fetch", true)) {
            Log.d("DeviceStatusReceiver", "rescheduling NotificationService...");

            new NotificationScheduler(context).schedule();
        }
    }
}
