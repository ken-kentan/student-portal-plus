package jp.kentan.studentportalplus.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.set
import androidx.core.text.toSpannable
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.PortalContent
import jp.kentan.studentportalplus.data.component.PortalData
import jp.kentan.studentportalplus.ui.lecturecancel.detail.LectureCancelDetailActivity
import jp.kentan.studentportalplus.ui.lectureinfo.detail.LectureInfoDetailActivity
import jp.kentan.studentportalplus.ui.login.LoginActivity
import jp.kentan.studentportalplus.ui.main.FragmentType
import jp.kentan.studentportalplus.ui.main.MainActivity
import jp.kentan.studentportalplus.ui.notice.detail.NoticeDetailActivity
import jp.kentan.studentportalplus.util.getNotificationId
import jp.kentan.studentportalplus.util.isEnabledNotificationLed
import jp.kentan.studentportalplus.util.isEnabledNotificationVibration
import jp.kentan.studentportalplus.util.setNotificationId
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.newTask

class NotificationController(context: Context) : ContextWrapper(context) {

    companion object {
        const val NEWLY_CHANNEL_ID = "0_newly_channel" //新着通知
        private const val APP_CHANNEL_ID = "99_app_channel"

        private const val GROUP_KEY = "student_portal_plus"
        private const val SUMMARY_NOTIFICATION_ID = 0
        private const val ERROR_NOTIFICATION_ID = -1
        private const val INBOX_LINE_LIMIT = 4

        private val CAN_USE_VECTOR_DRAWABLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        private val CAN_USE_SUMMARY = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        private val VIBRATION_PATTERN = longArrayOf(0, 300, 300, 300)

        private val SMALL_APP_ICON = if (CAN_USE_VECTOR_DRAWABLE) R.drawable.ic_menu_dashboard else R.mipmap.ic_notification_app
        private val SMALL_ICON_MAP = if (CAN_USE_VECTOR_DRAWABLE) {
            mapOf(
                    PortalData.LECTURE_INFO to R.drawable.ic_menu_lecture_info,
                    PortalData.LECTURE_CANCEL to R.drawable.ic_menu_lecture_cancel,
                    PortalData.NOTICE to R.drawable.ic_menu_notice
            )
        } else {
            mapOf(
                    PortalData.LECTURE_INFO to R.mipmap.ic_notification_lecture_info,
                    PortalData.LECTURE_CANCEL to R.mipmap.ic_notification_lecture_cancel,
                    PortalData.NOTICE to R.mipmap.ic_notification_notice
            )
        }


        fun setupChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return
            }

            val color = ContextCompat.getColor(context, R.color.colorAccent)

            val newlyChannel = NotificationChannel(NEWLY_CHANNEL_ID, context.getString(R.string.name_newly_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableLights(true)
                lightColor = color
                vibrationPattern = VIBRATION_PATTERN
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val appChannel = NotificationChannel(APP_CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(true)
                lightColor = color
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(listOf(newlyChannel, appChannel))
        }
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    private val isEnabledVibration = context.defaultSharedPreferences.isEnabledNotificationVibration()
    private val isEnabledLed = context.defaultSharedPreferences.isEnabledNotificationLed()

    private val accentColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val largeIcon by lazy(LazyThreadSafetyMode.NONE) { BitmapFactory.decodeResource(context.resources, R.mipmap.ic_notification_large) }

    private var isFirstNotify = true


    init {
        setupChannel(context)
    }

    fun notify(type: PortalData, contentList: List<PortalContent>) {
        if (contentList.isEmpty()) {
            return
        }

        val smallIcon = SMALL_ICON_MAP[type] ?: SMALL_APP_ICON
        val name = getString(type.nameResId)
        var id = defaultSharedPreferences.getNotificationId()

        if (CAN_USE_SUMMARY) {
            createSummaryNotification()

            val builder = NotificationCompat.Builder(applicationContext, NEWLY_CHANNEL_ID)

            contentList.forEach {
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(smallIcon)
                        .setGroup(GROUP_KEY)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                        .setGroupSummary(false)
                        .setSubText(name)
                        .setContentTitle(it.title)
                        .setContentText(it.text)
                        .setContentIntent(it.createIntent(type, id))
                        .setAutoCancel(true)

                notificationManager.notify(id, builder.build())

                if (++id >= Int.MAX_VALUE) {
                    id = 1
                }
            }
        } else {
            val title = if (contentList.size > 1) getString(R.string.title_content_inbox, contentList.size, name) else name

            val builder = NotificationCompat.Builder(applicationContext, NEWLY_CHANNEL_ID)
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
                builder.setVibrate(if (isEnabledVibration) VIBRATION_PATTERN else longArrayOf(0))

                if (isEnabledLed) {
                    builder.setLights(accentColor, 1000, 2000)
                }
            }

            notificationManager.notify(id, builder.build())

            if (++id >= Int.MAX_VALUE) {
                id = 1
            }
        }

        isFirstNotify = false
        defaultSharedPreferences.setNotificationId(id)
    }

    fun notifyError(message: String, isRequireLogin: Boolean = false) {
        val intent = if (isRequireLogin) {
            LoginActivity.createIntent(this, true)
        } else {
            MainActivity.createIntent(this)
        }

        val pendingIntent = PendingIntent.getActivity(this, ERROR_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(applicationContext, APP_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(ContextCompat.getColor(this, R.color.red_600))
                .setSmallIcon(SMALL_APP_ICON)
                .setSubText(getString(R.string.sub_text_sync_error))
                .setContentTitle(getString(if (isRequireLogin) R.string.title_require_login else R.string.title_error))
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        if (CAN_USE_VECTOR_DRAWABLE) {
            val retryService = PendingIntent.getService(this,
                    ERROR_NOTIFICATION_ID,
                    Intent(this, RetryActionService::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT)
            val retryAction = NotificationCompat.Action.Builder(R.drawable.ic_retry, getString(R.string.name_retry), retryService).build()

            builder.addAction(retryAction)
        }

        notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build())
    }

    fun cancelErrorNotification() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID)
    }

    private fun createSummaryNotification() {
        if (!CAN_USE_SUMMARY || !isFirstNotify) {
            return
        }

        val builder = NotificationCompat.Builder(applicationContext, NEWLY_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(SMALL_APP_ICON)
                .setColor(accentColor)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setVibrate(if (isEnabledVibration) VIBRATION_PATTERN else longArrayOf(0))
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)

        if (isEnabledLed) {
            builder.setLights(accentColor, 1000, 2000)
        }

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, builder.build())
    }

    private fun PortalContent.createIntent(type: PortalData, code: Int): PendingIntent? {
        val context = this@NotificationController

        val intent = when (type) {
            PortalData.LECTURE_INFO -> LectureInfoDetailActivity.createIntent(context, id)
            PortalData.LECTURE_CANCEL -> LectureCancelDetailActivity.createIntent(context, id)
            PortalData.NOTICE -> NoticeDetailActivity.createIntent(context, id)
            else -> return null
        }

        return PendingIntent.getActivity(context, code, intent.clearTask().newTask(), PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun PortalData.createIntent(id: Int): PendingIntent {
        val context = this@NotificationController

        val fragment = when (this) {
            PortalData.NOTICE -> FragmentType.NOTICE
            PortalData.LECTURE_INFO -> FragmentType.LECTURE_INFO
            PortalData.LECTURE_CANCEL -> FragmentType.LECTURE_CANCEL
            PortalData.MY_CLASS -> FragmentType.DASHBOARD // not support
        }

        val intent = MainActivity.createIntent(context, fragment = fragment)
        return PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun List<PortalContent>.createInboxStyle(type: PortalData): NotificationCompat.InboxStyle {
        val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(getString(type.nameResId))

        take(INBOX_LINE_LIMIT).forEach {
            inboxStyle.addLine(it.toInboxStyleText())
        }

        val moreContentSize = size - INBOX_LINE_LIMIT
        if (moreContentSize > 0) {
            inboxStyle.setSummaryText(getString(R.string.summary_text_inbox, moreContentSize))
        }

        return inboxStyle
    }

    private fun PortalContent.toInboxStyleText(): Spannable {
        val text = "$title $text".toSpannable()
        text[0..title.length] = StyleSpan(Typeface.BOLD)
        return text
    }
}