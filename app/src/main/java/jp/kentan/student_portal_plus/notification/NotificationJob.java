package jp.kentan.student_portal_plus.notification;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import jp.kentan.student_portal_plus.data.PortalDataProvider;

import static jp.kentan.student_portal_plus.notification.NotificationService.NOTIFY_ALL;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NotificationJob extends JobService implements PortalDataProvider.Callback {

    private final static String TAG = "NotificationJob";

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);

    private JobParameters mParams;

    private SharedPreferences mPreferences;
    private NotificationController mNotification;


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "job start.");

        if (!PortalDataProvider.isFetching()) {
            this.mParams = params;

            mNotification = new NotificationController(getApplicationContext());
            mNotification.cancel(NotificationController.ERROR_ID);

            mPreferences = getSharedPreferences("notification", MODE_PRIVATE);
            new PortalDataProvider(getApplicationContext(), this).fetch();

            return true;
        }

        Log.d(TAG, "job canceled.");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.w(TAG, "job stopped.");
        jobFinished(params, false);
        return false;
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
        jobFinished(mParams, false);
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

        saveLastAutoFetchedDate();
        jobFinished(mParams, false);

        Log.d(TAG, "job success.");
    }

    private void saveLastAutoFetchedDate() {
        SharedPreferences.Editor editor = getSharedPreferences("common", MODE_PRIVATE).edit();
        editor.putString("last_auto_fetched_date", DATE_FORMAT.format(Calendar.getInstance().getTime()));
        editor.apply();
    }
}
