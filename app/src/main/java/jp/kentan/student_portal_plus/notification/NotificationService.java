package jp.kentan.student_portal_plus.notification;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import jp.kentan.student_portal_plus.data.PortalDataProvider;


public class NotificationService extends Service implements PortalDataProvider.Callback {

    private final static String TAG = "NotificationService";

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);

    private final static boolean IS_SUPPORT_JOBSCHEDULER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public final static int NOTIFY_ALL = 0, NOTIFY_WITH_MY_CLASS = 1;

    private SharedPreferences mPreferences;
    private NotificationController mNotification;


    public NotificationService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!PortalDataProvider.isFetching() && ((intent != null && intent.getBooleanExtra("retry", false)) || !isMidnight())) {
            if(IS_SUPPORT_JOBSCHEDULER){
                final ComponentName component = new ComponentName(getApplicationContext(), NotificationJob.class);

                JobScheduler scheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                scheduler.cancelAll();

                JobInfo job = new JobInfo.Builder(0x01, component)
                        .setMinimumLatency(10)
                        .setOverrideDeadline(5000)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)//>adb shell dumpsys jobscheduler
                        .build();

                final int result = scheduler.schedule(job);

                if(result == JobScheduler.RESULT_SUCCESS){
                    Log.d(TAG, "Job scheduled.");
                    stopSelf();
                }else{
                    failed("Job schedule failed.", null);
                }
            }else{
                mNotification = new NotificationController(getApplicationContext());
                mNotification.cancel(NotificationController.ERROR_ID);

                mPreferences = getSharedPreferences("notification", MODE_PRIVATE);
                new PortalDataProvider(getApplicationContext(), this).fetch();
            }
        } else {
            stopSelf();
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void success() {
        /*
        Notify type (-1:none 0:all 1:my class)
         */
        final int lectureInfoType = mPreferences.getInt("type_lecture_info", NOTIFY_ALL); //-1 or 0 or 1
        final int cancelInfoType  = mPreferences.getInt("type_cancel_info" , NOTIFY_ALL); //-1 or 0 or 1
        final int latestInfoType  = mPreferences.getInt("type_latest_info" , NOTIFY_ALL); //-1 or 0

        if (lectureInfoType >= NOTIFY_ALL) {
            mNotification.notify(Content.TYPE.LECTURE_INFO, PortalDataProvider.getFetchedLectureInfoList(lectureInfoType));
        }

        if (cancelInfoType >= NOTIFY_ALL) {
            mNotification.notify(Content.TYPE.LECTURE_CANCEL, PortalDataProvider.getFetchedLectureCancelList(cancelInfoType));
        }

        if (latestInfoType >= NOTIFY_ALL) {
            mNotification.notify(Content.TYPE.NEWS, PortalDataProvider.getFetchedNews());
        }

        mNotification.destroy();

        saveLastAutoFetchedDate(getApplicationContext());
        stopSelf();
    }

    @Override
    public void failed(String errorMessage, Throwable error) {
        final SharedPreferences pref = getSharedPreferences("common", MODE_PRIVATE);

        if (pref.getBoolean("detail_err_msg", false)) {
            NotificationController notification = new NotificationController(getApplicationContext());
            notification.notifyAutoFetchFailed(errorMessage);
            notification.destroy();
        }

        Log.e(TAG, errorMessage);
        stopSelf();
    }

    private boolean isMidnight() {
        int now = Calendar.getInstance(Locale.JAPAN).get(Calendar.HOUR_OF_DAY);

        return (now < 5 || now >= 23);
    }

    static void saveLastAutoFetchedDate(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("common", MODE_PRIVATE).edit();
        editor.putString("last_auto_fetched_date", DATE_FORMAT.format(Calendar.getInstance().getTime()));
        editor.apply();
    }
}
