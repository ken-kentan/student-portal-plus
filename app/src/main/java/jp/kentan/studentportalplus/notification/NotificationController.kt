package jp.kentan.studentportalplus.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface.BOLD
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.style.StyleSpan
import androidx.core.text.set
import androidx.core.text.toSpannable
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.NotifyContent
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.ui.*
import jp.kentan.studentportalplus.util.enabledNotificationLed
import jp.kentan.studentportalplus.util.enabledNotificationVibration
import jp.kentan.studentportalplus.util.getNotificationId
import jp.kentan.studentportalplus.util.setNotificationId
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class NotificationController(
        private val context: Context
) {

    companion object {
        const val NEWLY_CHANNEL_ID = "0_newly_channel" //新着通知
        private const val APP_CHANNEL_ID = "99_app_channel"

        private const val GROUP_KEY = "student_portal_plus"
        private const val SUMMARY_NOTIFICATION_ID =  0
        private const val ERROR_NOTIFICATION_ID   = -1
        private const val INBOX_LINE_LIMIT        =  4

        private val CAN_USE_VECTOR_DRAWABLE      = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        private val CAN_USE_NOTIFICATION_SUMMARY = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        private val CAN_USE_NOTIFICATION_CHANNEL = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        private val VIBRATION_PATTERN = longArrayOf(0, 300, 300, 300)

        private val SMALL_APP_ICON = if (CAN_USE_VECTOR_DRAWABLE) R.drawable.ic_menu_dashboard else R.mipmap.ic_notification_app
        private val SMALL_ICON_MAP = if (CAN_USE_VECTOR_DRAWABLE) {
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

        fun setupChannel(context: Context) {
            if (!CAN_USE_NOTIFICATION_CHANNEL) { return }

            val color = ContextCompat.getColor(context, R.color.colorAccent)

            val newlyChannel = NotificationChannel(NEWLY_CHANNEL_ID, "新着通知", NotificationManager.IMPORTANCE_DEFAULT)
            newlyChannel.enableLights(true)
            newlyChannel.lightColor = color
            newlyChannel.vibrationPattern = VIBRATION_PATTERN
            newlyChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            val appChannel = NotificationChannel(APP_CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
            appChannel.enableLights(true)
            appChannel.lightColor = color
            appChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(listOf(newlyChannel, appChannel))
        }
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    private val enabledVibration = context.defaultSharedPreferences.enabledNotificationVibration()
    private val enabledLed       = context.defaultSharedPreferences.enabledNotificationLed()

    private val accentColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val largeIcon by lazy(LazyThreadSafetyMode.NONE) { BitmapFactory.decodeResource(context.resources, R.mipmap.ic_notification_large) }

    private var isFirstNotify = true


    fun notify(type: PortalDataType, contentList: List<NotifyContent>) {
        if (contentList.isEmpty()) {
            return
        }

        val smallIcon = SMALL_ICON_MAP[type] ?: SMALL_APP_ICON
        var id = context.defaultSharedPreferences.getNotificationId()

        if (CAN_USE_NOTIFICATION_SUMMARY) {
            createSummaryNotification()

            contentList.forEach {
                val builder = NotificationCompat.Builder(context, NEWLY_CHANNEL_ID)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(smallIcon)
                        .setGroup(GROUP_KEY)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                        .setGroupSummary(false)
                        .setSubText(type.displayName)
                        .setContentTitle(it.title)
                        .setContentText(it.text)
                        .setContentIntent(it.createIntent(type, id))
                        .setAutoCancel(true)

                notificationManager.notify(id, builder.build())

                if (++id >= Int.MAX_VALUE) { id = 1 }
            }
        } else {
            val title = if (contentList.size > 1) "${contentList.size}件の${type.displayName}" else type.displayName

            val builder = NotificationCompat.Builder(context, NEWLY_CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(smallIcon)
                    .setColor(accentColor)
                    .setContentTitle(title)
                    .setContentText(contentList.first().toInboxStyleText())
                    .setContentIntent(type.createIntent(id))
                    .setStyle(contentList.createInboxStyle(type))
                    .setAutoCancel(true)

            if (isFirstNotify) {
                builder.setVibrate(if (enabledVibration) VIBRATION_PATTERN else longArrayOf(0))

                if (enabledLed) {
                    builder.setLights(accentColor, 1000, 2000)
                }
            }

            notificationManager.notify(id, builder.build())

            if (++id >= Int.MAX_VALUE) { id = 1 }
        }

        isFirstNotify = false
        context.defaultSharedPreferences.setNotificationId(id)
    }

    fun notifyError(message: String, isRequireLogin: Boolean = false) {
        val activity = if (isRequireLogin) {
            context.intentFor<LoginActivity>(LoginActivity.LAUNCH_MAIN_ACTIVITY to true)
        } else {
            context.intentFor<MainActivity>()
        }
        val intent = PendingIntent.getActivity(context, ERROR_NOTIFICATION_ID, activity, FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, APP_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(context, R.color.red_600))
                .setSmallIcon(SMALL_APP_ICON)
                .setSubText("同期失敗")
                .setContentTitle(if (isRequireLogin) "再ログインが必要です" else "エラー")
                .setContentText(message)
                .setContentIntent(intent)
                .setAutoCancel(true)

        if (CAN_USE_VECTOR_DRAWABLE) {
            val retryService = PendingIntent.getService(context, ERROR_NOTIFICATION_ID, context.intentFor<RetryActionService>(), FLAG_UPDATE_CURRENT)
            val retryAction = NotificationCompat.Action.Builder(R.drawable.ic_refresh, "再試行", retryService).build()

            builder.addAction(retryAction)
        }

        notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build())
    }

    fun cancelErrorNotification() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID)
    }

    private fun createSummaryNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !isFirstNotify) {
            return
        }

        val summary = NotificationCompat.Builder(context, NEWLY_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(SMALL_APP_ICON)
                .setColor(accentColor)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setVibrate(if (enabledVibration) VIBRATION_PATTERN else longArrayOf(0))
                .setLights(accentColor, 1000, 2000)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)

        if (enabledLed) {
            summary.setLights(accentColor, 1000, 2000)
        }

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, summary.build())
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

    private fun PortalDataType.createIntent(id: Int): PendingIntent {
        val fragmentType = when (this) {
            PortalDataType.NOTICE               -> MainActivity.FragmentType.NOTICE
            PortalDataType.LECTURE_INFORMATION  -> MainActivity.FragmentType.LECTURE_INFO
            PortalDataType.LECTURE_CANCELLATION -> MainActivity.FragmentType.LECTURE_CANCEL
            PortalDataType.MY_CLASS             -> MainActivity.FragmentType.DASHBOARD // not support
        }

        val intent = context.intentFor<MainActivity>(MainActivity.FRAGMENT_TYPE to fragmentType.name).clearTop().newTask()

        return PendingIntent.getActivity(context, id, intent, FLAG_UPDATE_CURRENT)
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
        val text = "$title $text".toSpannable()
        text[0..title.length] = StyleSpan(BOLD)
        return text
    }
}