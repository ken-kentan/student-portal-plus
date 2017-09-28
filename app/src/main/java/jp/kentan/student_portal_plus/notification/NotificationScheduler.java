package jp.kentan.student_portal_plus.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;


public class  NotificationScheduler {

    private final static String TAG = "NotificationScheduler";
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);

    private final static boolean IS_SUPPORT_JOBSCHEDULER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    private final Context mContext;


    public NotificationScheduler(Context context) {
        mContext = context;
    }

    public void scheduleIfNeed() {
        final SharedPreferences pref = mContext.getSharedPreferences("common", MODE_PRIVATE);

        if (!pref.getBoolean("auto_fetch", true)) return;

        try {
            final Date dateFrom = DATE_FORMAT.parse(pref.getString("last_auto_fetched_date", "2017-01-01 00:00:00"));
            final Date dateTo = Calendar.getInstance().getTime();

            long diffHours = (dateTo.getTime() - dateFrom.getTime()) / (1000 * 60 * 60);

            if (diffHours > 12) {
                schedule();
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    public void schedule() {
        final SharedPreferences pref = mContext.getSharedPreferences("common", MODE_PRIVATE);
        schedule(pref.getInt("auto_fetch_interval_m", 60));
    }

    public void schedule(int intervalMinutes) {
        long intervalMillis = (long)intervalMinutes * 60 * 1000;
        long triggerAtMillis = SystemClock.elapsedRealtime() + intervalMillis;

        cancel();

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, intervalMillis, getNotificationIntent());

        Log.d(TAG, "NotificationService scheduled. (INTERVAL: " + intervalMinutes + "m)");
    }

    public void cancel() {
        try {
            final PendingIntent service = getNotificationIntent();

            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            service.cancel();
            alarmManager.cancel(service);

            if(IS_SUPPORT_JOBSCHEDULER){
                JobScheduler scheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                scheduler.cancelAll();
            }

            Log.d(TAG, "NotificationService canceled.");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private PendingIntent getNotificationIntent() {
        final Intent intent = new Intent(mContext, NotificationService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
