package jp.kentan.studentportalplus.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.Preferences
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.ui.lecturecancellationdetail.LectureCancellationDetailActivity
import jp.kentan.studentportalplus.ui.lectureinformationdetail.LectureInformationDetailActivity
import jp.kentan.studentportalplus.ui.noticedetail.NoticeDetailActivity

@RequiresApi(Build.VERSION_CODES.N)
class SummaryNotificationHelper(
    context: Context,
    private val preferences: Preferences
) : NotificationHelper(context) {

    companion object {
        private const val GROUP_KEY = "student_portal_plus"
        private const val SUMMARY_NOTIFICATION_ID = 0

        private const val SMALL_APP_ICON_RES_ID = R.drawable.notification_app

        @RequiresApi(Build.VERSION_CODES.O)
        fun createNewlyChannelSettingsIntent(context: Context) =
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, NEWLY_CHANNEL_ID)
            }
    }

    private inner class NotificationContent(
        val title: String,
        val text: String?,
        private val intent: Intent
    ) {
        fun createPendingIntent(notificationId: Int): PendingIntent = intent.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }.toPendingIntent(notificationId)
    }

    private val color = context.getColor(R.color.notification)
    private val colorLight = context.getColor(R.color.notification_light)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChannels() {
        val newlyChannel = NotificationChannel(
            NEWLY_CHANNEL_ID,
            context.getString(R.string.notification_newly_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableLights(true)
            lightColor = colorLight
            vibrationPattern = VIBRATION_PATTERN
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val appChannel = NotificationChannel(
            APP_CHANNEL_ID,
            context.getString(R.string.notification_app_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            enableLights(true)
            lightColor = colorLight
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        notificationManager.createNotificationChannels(listOf(newlyChannel, appChannel))
    }

    override fun sendLectureInformation(lectureInfoList: List<LectureInformation>) {
        if (lectureInfoList.isEmpty()) {
            return
        }

        val contentList = lectureInfoList.map {
            NotificationContent(
                title = it.subject,
                text = it.detailText,
                intent = LectureInformationDetailActivity.createIntent(context, it.id)
            )
        }

        sendContentToNewlyChannel(
            R.drawable.notification_lecture_information,
            R.string.all_lecture_information,
            contentList
        )
    }

    override fun sendLectureCancellation(lectureCancelList: List<LectureCancellation>) {
        if (lectureCancelList.isEmpty()) {
            return
        }

        val contentList = lectureCancelList.map {
            NotificationContent(
                title = it.subject,
                text = it.detailText,
                intent = LectureCancellationDetailActivity.createIntent(context, it.id)
            )
        }

        sendContentToNewlyChannel(
            R.drawable.notification_lecture_cancellation,
            R.string.all_lecture_cancellation,
            contentList
        )
    }

    override fun sendNotice(noticeList: List<Notice>) {
        if (noticeList.isEmpty()) {
            return
        }

        val contentList = noticeList.map {
            NotificationContent(
                title = it.title,
                text = it.detailText ?: it.link,
                intent = NoticeDetailActivity.createIntent(context, it.id)
            )
        }

        sendContentToNewlyChannel(
            R.drawable.notification_notice,
            R.string.all_notice,
            contentList
        )
    }

    private fun sendContentToNewlyChannel(
        @DrawableRes smallIconResId: Int,
        @StringRes subTextResId: Int,
        contentList: List<NotificationContent>
    ) {
        sendGroupSummary()

        var notificationId = preferences.notificationId

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

        preferences.notificationId = notificationId
    }

    private fun sendGroupSummary() {
        val builder = NotificationCompat.Builder(context, NEWLY_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(SMALL_APP_ICON_RES_ID)
            .setColor(color)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        // If notification chanel not supported
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val vibratePattern = if (preferences.isNotificationVibrationEnabled) {
                VIBRATION_PATTERN
            } else {
                longArrayOf(0)
            }

            builder.setVibrate(vibratePattern)

            if (preferences.isNotificationLedEnabled) {
                builder.setLights(
                    colorLight,
                    NOTIFICATION_LED_ON_MILLIS,
                    NOTIFICATION_LED_OFF_MILLIS
                )
            }
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
