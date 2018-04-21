package jp.kentan.studentportalplus.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_NO_CREATE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import androidx.core.content.edit
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.NotifyContent
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.ui.LectureInformationActivity
import jp.kentan.studentportalplus.ui.LoginActivity
import jp.kentan.studentportalplus.ui.MainActivity
import jp.kentan.studentportalplus.ui.NoticeActivity
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class NotificationController(
        private val context: Context
) {

    private companion object {
        const val CHANNEL_ID = "channel"
        const val GROUP_KEY = "student_portal_plus"
        const val SUMMARY_NOTIFICATION_ID =  0
        const val ERROR_NOTIFICATION_ID   = -1

        val VIBRATION_PATTERN = longArrayOf(0, 300, 300, 300)

        const val APP_ICON = R.drawable.ic_menu_dashboard

        val SMALL_ICON_MAP = mapOf(
                PortalDataType.LECTURE_INFORMATION  to R.drawable.ic_menu_lecture_info,
                PortalDataType.LECTURE_CANCELLATION to R.drawable.ic_menu_lecture_cancel,
                PortalDataType.NOTICE               to R.drawable.ic_menu_notice
        )
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    private val enableVibration = context.defaultSharedPreferences.getBoolean("enable_notify_vibration", true)
    private val enableLed       = context.defaultSharedPreferences.getBoolean("enable_notify_led", true)

    private var isFirstNotify = true

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "新着通知", NotificationManager.IMPORTANCE_DEFAULT)
            channel.setShowBadge(true)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun notify(type: PortalDataType, contentList: List<NotifyContent>) {
        if (contentList.isEmpty()) {
            return
        }

        val smallIcon = SMALL_ICON_MAP[type] ?: APP_ICON
        var id = context.defaultSharedPreferences.getInt("notification_id", 1)

        createSummaryNotification()

        contentList.forEach {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(smallIcon)
                    .setGroup(GROUP_KEY)
                    .setSubText(type.displayName)
                    .setContentTitle(it.title)
                    .setContentText(it.text)
                    .setContentIntent(it.getIntent(type, id))

            notificationManager.notify(id, build(builder))

            if (++id >= Int.MAX_VALUE) { id = 1 }
        }

        context.defaultSharedPreferences.edit {
            putInt("notification_id", id)
        }
    }

    fun notifyError(message: String, isRequireLogin: Boolean = false) {
        val activity = if (isRequireLogin) {
            context.intentFor<LoginActivity>("request_launch_main_activity" to true)
        } else {
            context.intentFor<MainActivity>()
        }
        val intent = PendingIntent.getActivity(context, ERROR_NOTIFICATION_ID, activity, FLAG_UPDATE_CURRENT)

        val retryService = PendingIntent.getService(context, ERROR_NOTIFICATION_ID, context.intentFor<RetryActionService>(), FLAG_UPDATE_CURRENT)
        val retryAction = NotificationCompat.Action.Builder(R.drawable.ic_refresh, "再試行", retryService).build()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(APP_ICON)
                .setSubText("同期失敗")
                .setContentTitle(if (isRequireLogin) "再ログインが必要です" else "エラー")
                .setContentText(message)
                .setContentIntent(intent)
                .addAction(retryAction)
                .setAutoCancel(true)

        notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build())
    }

    fun cancelErrorNotification() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID)
    }

    private fun build(builder: NotificationCompat.Builder): Notification {
        if (isFirstNotify) {
            builder.setVibrate(if (enableVibration) VIBRATION_PATTERN else longArrayOf(0))

            if (enableLed) {
                builder.setLights(ContextCompat.getColor(context, R.color.colorAccent), 1000, 2000)
            }

            isFirstNotify = false
        }

        builder.setAutoCancel(true)

        return builder.build()
    }

    private fun createSummaryNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !isFirstNotify) {
            return
        }

        val intent = PendingIntent.getActivity(context, SUMMARY_NOTIFICATION_ID, context.intentFor<MainActivity>(), FLAG_NO_CREATE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(APP_ICON)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, notification)
    }


    private fun NotifyContent.getIntent(type: PortalDataType, code: Int): PendingIntent? {
        val intent = when (type) {
            PortalDataType.LECTURE_INFORMATION  -> context.intentFor<LectureInformationActivity>("id" to id)
            PortalDataType.LECTURE_CANCELLATION -> context.intentFor<LectureInformationActivity>("id" to id)
            PortalDataType.NOTICE               -> context.intentFor<NoticeActivity>("id" to id)
            else -> return null
        }

        return PendingIntent.getActivity(context, code, intent.clearTop().newTask(), FLAG_UPDATE_CURRENT)
    }
}