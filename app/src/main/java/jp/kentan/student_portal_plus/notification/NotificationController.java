package jp.kentan.student_portal_plus.notification;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Spanned;
import android.util.Log;

import java.util.List;

import jp.kentan.student_portal_plus.ui.HomeActivity;
import jp.kentan.student_portal_plus.ui.LectureCancellationActivity;
import jp.kentan.student_portal_plus.ui.NewsActivity;
import jp.kentan.student_portal_plus.ui.LectureInformationActivity;
import jp.kentan.student_portal_plus.R;

public class NotificationController {

    private final static long[] VIBRATE = {0, 300, 300, 300};

    private final static String GROUP_KEY = "student_portal_plus";

    private final static int INBOX_LINE_LIMIT = 4;
    final static int ERROR_ID = -1;

    private static Bitmap LARGE_ICON;
    private static int APP_ICON;
    private static int[] SMALL_ICON = new int[3];

    private static int COLOR_ACCENT;
    private final boolean isVibrate, isLed;

    private NotificationManagerCompat mNotificationManager;
    private Context mContext;
    private SharedPreferences mPreference;

    private final static boolean IS_SUPPORT_NOTIFICATION_SUMMARY = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
    private final static boolean IS_SUPPORT_VECTOR_IMG           = (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT);
    private boolean isFirstNotify = true, isSummaryAvailable = false;

    private final static int SUMMARY_NOTIFY_ID = 0;
    private int mLastNotifyId = 1; //IS_SUPPORT_NOTIFICATION_SUMMARY
    private final int[] NOTIFY_ID = {1/* 授業関連連絡 */, 2/* 休講情報 */, 3/* 最新情報 */};
    private final int[] VIEWMODE  = {3/* 授業関連連絡 */, 4/* 休講情報 */, 5/* 最新情報 */};


    public NotificationController(Context context) {
        mContext = context.getApplicationContext();

        mPreference = mContext.getSharedPreferences("notification", Context.MODE_PRIVATE);

        isVibrate = mPreference.getBoolean("vibrate", true);
        isLed     = mPreference.getBoolean("led"    , true);

        mLastNotifyId = mPreference.getInt("last_notify_id", 1);

        if(mLastNotifyId <= SUMMARY_NOTIFY_ID) mLastNotifyId = 1;

        LARGE_ICON = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_notify);

        //Check vector image support
        if (IS_SUPPORT_VECTOR_IMG) {
            APP_ICON = R.drawable.ic_menu_dashboard;

            SMALL_ICON[0] = R.drawable.ic_menu_class_schedule_change;
            SMALL_ICON[1] = R.drawable.ic_menu_class_cancel;
            SMALL_ICON[2] = R.drawable.ic_menu_news_events;
        } else {
            APP_ICON = R.mipmap.ic_app;

            SMALL_ICON[0] = R.mipmap.ic_class_schedule_change;
            SMALL_ICON[1] = R.mipmap.ic_class_cancel;
            SMALL_ICON[2] = R.mipmap.ic_news_events;
        }

        COLOR_ACCENT = ResourcesCompat.getColor(mContext.getResources(), R.color.colorAccent, null);

        mNotificationManager = NotificationManagerCompat.from(mContext);
    }

    public void cancel(int notifyId) {
        mNotificationManager.cancel(notifyId);
    }

    public void cancelAll() {
        mNotificationManager.cancelAll();

        SharedPreferences.Editor editor = mPreference.edit();
        editor.putInt("last_notify_id", 1);
        editor.apply();
    }

    void destroy() {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putInt("last_notify_id", mLastNotifyId);
        editor.apply();
    }

    void notify(Content.TYPE type, List<Content> list){
        if(list.size() <= 0) return;

        final int intType = type.ordinal();

        if (IS_SUPPORT_NOTIFICATION_SUMMARY) {
            createNotificationSummaryIfNeed();

            final String subText = Content.NAME[intType];

            for(Content content : list){
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSmallIcon(SMALL_ICON[intType])
                        .setGroup(GROUP_KEY)
                        .setSubText(subText)
                        .setContentTitle(content.getTitle())
                        .setContentText(content.getText())
                        .setContentIntent(createPendingIntent(content));

                mNotificationManager.notify(mLastNotifyId++, build(builder));
            }
        } else {
            Intent intent = new Intent(mContext, HomeActivity.class);
            intent.putExtra("view_mode", VIEWMODE[intType]);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext, NOTIFY_ID[intType], intent, 0);

            NotificationCompat.Builder builder = getNotificationBuilder(type, Content.getInboxStyleTitle(type, list.size()), list.get(0).getInboxStyleText(), contentIntent);

            NotificationCompat.InboxStyle inbox = getInboxStyle(Content.NAME[intType], list);
            builder.setStyle(inbox);

            mNotificationManager.notify(NOTIFY_ID[intType], build(builder));
        }
    }

    void notifyAutoFetchFailed(String errorMessage) {
        mLastNotifyId++;

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, mLastNotifyId, new Intent(mContext, HomeActivity.class), 0);

        Intent service = new Intent(mContext, NotificationService.class);
        service.putExtra("retry", true);

        PendingIntent serviceIntent = PendingIntent.getService(mContext, ERROR_ID, service, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionRetry = new NotificationCompat.Action.Builder(R.drawable.ic_refresh, "再試行", serviceIntent).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(APP_ICON)
                .setContentTitle("自動受信失敗")
                .setContentText(errorMessage)
                .setContentIntent(contentIntent);

        if(IS_SUPPORT_VECTOR_IMG){
            builder.addAction(actionRetry);
        }

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(ERROR_ID, notification);
    }

    private void createNotificationSummaryIfNeed() {
        if (!IS_SUPPORT_NOTIFICATION_SUMMARY || isSummaryAvailable) return;
        isSummaryAvailable = true;

        final PendingIntent contentIntent = PendingIntent.getActivity(mContext, SUMMARY_NOTIFY_ID, new Intent(mContext, HomeActivity.class), 0);

        Notification summaryNotification = new NotificationCompat.Builder(mContext)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(APP_ICON)
                .setColor(COLOR_ACCENT)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();
        mNotificationManager.notify(SUMMARY_NOTIFY_ID, summaryNotification);
    }

    private PendingIntent createPendingIntent(Content content){
        final Intent intent;

        switch (content.getType()){
            case LECTURE_INFO:
                intent = new Intent(mContext, LectureInformationActivity.class);
                break;
            case LECTURE_CANCEL:
                intent = new Intent(mContext, LectureCancellationActivity.class);
                break;
            case NEWS:
                intent = new Intent(mContext, NewsActivity.class);
                break;
            default:
                return null;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("hash", content.getHash());
        intent.putExtra("notify_id", mLastNotifyId);
        intent.putExtra("notification", true);

        Log.d("Notify", mLastNotifyId + ":" + content.getTitle());

        return PendingIntent.getActivity(mContext, mLastNotifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private NotificationCompat.InboxStyle getInboxStyle(String title, List<Content> list) {
        final int size = list.size();

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);

        for (int i = 0; i < size; ++i) {
            inboxStyle.addLine(list.get(i).getInboxStyleText());
            if (i >= INBOX_LINE_LIMIT - 1) break;
        }

        final int more = size - INBOX_LINE_LIMIT;
        if (more > 0) {
            inboxStyle.setSummaryText("他" + more + "件");
        }

        return inboxStyle;
    }

    private NotificationCompat.Builder getNotificationBuilder(Content.TYPE type, String title, Spanned text, PendingIntent intent) {
        return new NotificationCompat.Builder(mContext)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setLargeIcon(LARGE_ICON)
                .setSmallIcon(SMALL_ICON[type.ordinal()])
                .setColor(COLOR_ACCENT)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(intent);
    }

    private Notification build(NotificationCompat.Builder builder) {
        Notification notification;

        if (isFirstNotify) {
            isFirstNotify = false;

            if (isLed) builder.setLights(COLOR_ACCENT, 1000, 2000);
            builder.setVibrate((isVibrate) ? VIBRATE : new long[]{0});

            notification = builder.build();
            if (isLed) notification.flags = Notification.FLAG_SHOW_LIGHTS;
        } else {
            notification = builder.build();
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        return notification;
    }
}
