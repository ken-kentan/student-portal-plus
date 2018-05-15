package jp.kentan.studentportalplus.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_NO_CREATE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.content.edit
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.NotifyContent
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.ui.*
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
        const val INBOX_LINE_LIMIT        = 4

        val CAN_USE_VECTOR_DRAWABLE      = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        val CAN_USE_NOTIFICATION_SUMMARY = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        val VIBRATION_PATTERN = longArrayOf(0, 300, 300, 300)

        val SMALL_APP_ICON = if (CAN_USE_VECTOR_DRAWABLE) R.drawable.ic_menu_dashboard else R.mipmap.ic_notification_app
        val SMALL_ICON_MAP = if (CAN_USE_VECTOR_DRAWABLE) {
            mapOf(
                    PortalDataType.LECTURE_INFORMATION  to R.drawable.ic_menu_lecture_info,
                    PortalDataType.LECTURE_CANCELLATION to R.drawable.ic_menu_lecture_cancel,
                    PortalDataType.NOTICE               to R.drawable.ic_menu_notice
            )
        } else {
            mapOf(
                    PortalDataType.LECTURE_INFORMATION  to R.mipmap.ic_notification_lecture_info,
                    PortalDataType.LECTURE_CANCELLATION to R.mipmap.ic_notification_lecture_cancel,
                    PortalDataType.NOTICE               to R.mipmap.ic_notification_notice
            )
        }

        val FRAGMENT_TYPE_MAP by lazy { mapOf(
                PortalDataType.LECTURE_INFORMATION  to MainActivity.FragmentType.LECTURE_INFO,
                PortalDataType.LECTURE_CANCELLATION to MainActivity.FragmentType.LECTURE_CANCEL,
                PortalDataType.NOTICE               to MainActivity.FragmentType.NOTICE
        ) }
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    private val enableVibration = context.defaultSharedPreferences.getBoolean("enable_notify_vibration", true)
    private val enableLed       = context.defaultSharedPreferences.getBoolean("enable_notify_led", true)

    private val accentColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val largeIcon by lazy { BitmapFactory.decodeResource(context.resources, R.mipmap.ic_notification_large) }

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

        val smallIcon = SMALL_ICON_MAP[type] ?: SMALL_APP_ICON
        var id = context.defaultSharedPreferences.getInt("notification_id", 1)

        if (CAN_USE_NOTIFICATION_SUMMARY) {
            createSummaryNotification()

            contentList.forEach {
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(smallIcon)
                        .setGroup(GROUP_KEY)
                        .setSubText(type.displayName)
                        .setContentTitle(it.title)
                        .setContentText(it.text)
                        .setContentIntent(it.createIntent(type, id))

                notificationManager.notify(id, build(builder))

                if (++id >= Int.MAX_VALUE) { id = 1 }
            }
        } else {
            val fragmentType = FRAGMENT_TYPE_MAP[type] ?: MainActivity.FragmentType.DASHBOARD
            val intent = context.intentFor<MainActivity>("fragment_type" to fragmentType.name).clearTop().newTask()
            val activity = PendingIntent.getActivity(context, id, intent, FLAG_UPDATE_CURRENT)

            val title = if (contentList.size > 1) "${contentList.size}件の${type.displayName}" else type.displayName

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(smallIcon)
                    .setColor(accentColor)
                    .setContentTitle(title)
                    .setContentText(contentList.first().toInboxStyleText())
                    .setContentIntent(activity)
                    .setStyle(contentList.createInboxStyle(type))

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
                .setLargeIcon(largeIcon)
                .setSmallIcon(SMALL_APP_ICON)
                .setSubText("同期失敗")
                .setContentTitle(if (isRequireLogin) "再ログインが必要です" else "エラー")
                .setContentText(message)
                .setContentIntent(intent)
                .setAutoCancel(true)

        if (CAN_USE_VECTOR_DRAWABLE) {
            builder.addAction(retryAction)
        }

        notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build())
    }

    fun cancelErrorNotification() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID)
    }

    private fun build(builder: NotificationCompat.Builder): Notification {
        if (isFirstNotify) {
            builder.setVibrate(if (enableVibration) VIBRATION_PATTERN else longArrayOf(0))

            if (enableLed) {
                builder.setLights(accentColor, 1000, 2000)
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
                .setSmallIcon(SMALL_APP_ICON)
                .setColor(accentColor)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, notification)
    }

    private fun NotifyContent.createIntent(type: PortalDataType, code: Int): PendingIntent? {
        val intent = when (type) {
            PortalDataType.LECTURE_INFORMATION  -> context.intentFor<LectureInformationActivity>("id" to id)
            PortalDataType.LECTURE_CANCELLATION -> context.intentFor<LectureCancellationActivity>("id" to id)
            PortalDataType.NOTICE               -> context.intentFor<NoticeActivity>("id" to id)
            else -> return null
        }

        return PendingIntent.getActivity(context, code, intent.clearTop().newTask(), FLAG_UPDATE_CURRENT)
    }

    private fun List<NotifyContent>.createInboxStyle(type: PortalDataType): NotificationCompat.InboxStyle {
        val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(type.displayName)

        take(INBOX_LINE_LIMIT).forEach {
            inboxStyle.addLine(it.toInboxStyleText())
        }

        val moreContentSize = size - INBOX_LINE_LIMIT
        if (moreContentSize > 0) {
            inboxStyle.setSummaryText("他${moreContentSize}件")
        }

        return inboxStyle
    }

    private fun NotifyContent.toInboxStyleText(): Spannable {
        val text = SpannableString("$title $text")
        text.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return text
    }
}