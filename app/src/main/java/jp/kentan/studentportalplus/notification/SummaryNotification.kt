package jp.kentan.studentportalplus.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
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

        sendGroupSummaryIfNeeded()

        val builder = NotificationCompat.Builder(context, NEWLY_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_menu_notice)
            .setGroup(GROUP_KEY)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .setGroupSummary(false)
            .setSubText(context.getString(R.string.name_notice))
            .setAutoCancel(true)

        noticeList.forEach { notice ->
            val intent = NoticeDetailActivity.createIntent(context, notice.id).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            builder.setContentTitle(notice.title)
                .setContentText(notice.detailText ?: notice.link)
                .setContentIntent(intent.toPendingIntent(99))

            notificationManager.notify(99, builder.build())
        }
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

    private fun Intent.toPendingIntent(code: Int) = PendingIntent.getActivity(
        context,
        code,
        this,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
}
