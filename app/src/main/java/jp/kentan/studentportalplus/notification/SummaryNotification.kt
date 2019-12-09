package jp.kentan.studentportalplus.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.ui.noticedetail.NoticeDetailActivity
import java.util.concurrent.atomic.AtomicBoolean

@RequiresApi(Build.VERSION_CODES.N)
class SummaryNotification(
    private val context: Context,
    private val localPreferences: LocalPreferences
) : NotificationHelper() {

    companion object {
        private const val NEWLY_CHANNEL_ID = "0_newly_channel" // 新着通知
        private const val APP_CHANNEL_ID = "99_app_channel"

        private const val GROUP_KEY = "student_portal_plus"
        private const val SUMMARY_NOTIFICATION_ID = 0

        private const val SMALL_APP_ICON_RES_ID = R.drawable.ic_menu_dashboard
    }

    private inner class NotificationContent(
        val title: String,
        val text: String?,
        private val intent: Intent
    ) {
        fun createPendingIntent(notificationId: Int): PendingIntent =
            intent.toPendingIntent(notificationId)
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    private val color = ContextCompat.getColor(context, R.color.notification)

    private val isGroupSummarySent = AtomicBoolean()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupChannels() {
        val newlyChannel = NotificationChannel(
            NEWLY_CHANNEL_ID,
            context.getString(R.string.name_newly_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableLights(true)
            lightColor = color
            vibrationPattern = VIBRATION_PATTERN
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val appChannel = NotificationChannel(
            APP_CHANNEL_ID,
            context.getString(R.string.name_app_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            enableLights(true)
            lightColor = color
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        notificationManager.createNotificationChannels(listOf(newlyChannel, appChannel))
    }

    override fun sendLectureInformation(lectureInfoList: List<LectureInformation>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendLectureCancellation(lectureCancelList: List<LectureCancellation>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendNotice(noticeList: List<Notice>) {
        if (noticeList.isEmpty()) {
            return
        }

        val contentList = noticeList.map {
            val intent = NoticeDetailActivity.createIntent(context, it.id).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            NotificationContent(
                title = it.title,
                text = it.detailText ?: it.link,
                intent = intent
            )
        }

        sendContentToNewlyChannel(R.drawable.ic_menu_notice, R.string.name_notice, contentList)
    }

    private fun sendContentToNewlyChannel(
        @DrawableRes smallIconResId: Int,
        @StringRes subTextResId: Int,
        contentList: List<NotificationContent>
    ) {
        sendGroupSummaryIfNeeded()

        var notificationId = localPreferences.notificationId

        val builder = NotificationCompat.Builder(context, NEWLY_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(smallIconResId)
            .setGroup(GROUP_KEY)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .setGroupSummary(false)
            .setSubText(context.getString(subTextResId))
            .setAutoCancel(true)

        contentList.forEach {
            builder.setContentTitle(it.title)
                .setContentText(it.text)
                .setContentIntent(it.createPendingIntent(notificationId))

            notificationManager.notify(notificationId, builder.build())

            if (++notificationId >= Int.MAX_VALUE) {
                notificationId = 1
            }
        }

        localPreferences.notificationId = notificationId
    }

    private fun sendGroupSummaryIfNeeded() {
        if (isGroupSummarySent.getAndSet(true)) {
            return
        }

        val vibrate = if (localPreferences.isEnabledNotificationVibration) {
            VIBRATION_PATTERN
        } else {
            longArrayOf(0)
        }

        val builder = NotificationCompat.Builder(context, NEWLY_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(SMALL_APP_ICON_RES_ID)
            .setColor(color)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setVibrate(vibrate)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        if (localPreferences.isEnabledNotificationLed) {
            builder.setLights(color, 1000, 2000)
        }

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, builder.build())
    }

    private fun Intent.toPendingIntent(requestCode: Int) = PendingIntent.getActivity(
        context,
        requestCode,
        this,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
}
