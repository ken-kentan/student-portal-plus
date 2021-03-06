package jp.kentan.studentportalplus.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.SpannableStringBuilder
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.Preferences
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.ui.MainActivity

class InboxStyleNotificationHelper(
    context: Context,
    private val preferences: Preferences
) : NotificationHelper(context) {

    companion object {
        private const val INBOX_LINE_LIMIT = 4
    }

    private class NotificationContent(
        val title: String,
        private val text: String?
    ) {
        val styleText: CharSequence
            get() = SpannableStringBuilder()
                .bold {
                    append(title)
                }
                .append(' ')
                .append(text)
    }

    private val largeIcon: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.notification_large)

    private val color = ContextCompat.getColor(context, R.color.notification)

    private var isFirstNotification = true

    override fun sendLectureInformation(lectureInfoList: List<LectureInformation>) {
        if (lectureInfoList.isEmpty()) {
            return
        }

        val contentList = lectureInfoList.map {
            NotificationContent(it.subject, it.detailText)
        }

        send(
            R.drawable.notification_lecture_information,
            R.string.all_lecture_information,
            contentList,
            MainActivity.createIntent(context, R.id.lecture_informations_fragment)
        )
    }

    override fun sendLectureCancellation(lectureCancelList: List<LectureCancellation>) {
        if (lectureCancelList.isEmpty()) {
            return
        }

        val contentList = lectureCancelList.map {
            NotificationContent(it.subject, it.detailText)
        }

        send(
            R.drawable.notification_lecture_cancellation,
            R.string.all_lecture_cancellation,
            contentList,
            MainActivity.createIntent(context, R.id.lecture_cancellations_fragment)
        )
    }

    override fun sendNotice(noticeList: List<Notice>) {
        if (noticeList.isEmpty()) {
            return
        }

        val contentList = noticeList.map {
            NotificationContent(it.title, it.detailText ?: it.link)
        }

        send(
            R.drawable.notification_notice,
            R.string.all_notice,
            contentList,
            MainActivity.createIntent(context, R.id.notices_fragment)
        )
    }

    private fun send(
        @DrawableRes smallIconResId: Int,
        @StringRes titleResId: Int,
        contentList: List<NotificationContent>,
        intent: Intent
    ) {
        var notificationId = preferences.notificationId

        val title = context.getString(titleResId)
        val contentTitle =
            context.getString(R.string.notification_inbox_style, contentList.size, title)

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, NEWLY_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon(largeIcon)
            .setSmallIcon(smallIconResId)
            .setColor(color)
            .setContentTitle(contentTitle)
            .setContentText(contentList.first().styleText)
            .setContentIntent(pendingIntent)
            .setStyle(createInboxStyle(title, contentList))
            .setAutoCancel(true)

        if (isFirstNotification) {
            isFirstNotification = false

            val vibratePattern = if (preferences.isNotificationVibrationEnabled) {
                VIBRATION_PATTERN
            } else {
                longArrayOf(0)
            }

            builder.setVibrate(vibratePattern)

            if (preferences.isNotificationLedEnabled) {
                builder.setLights(color, NOTIFICATION_LED_ON_MILLIS, NOTIFICATION_LED_OFF_MILLIS)
            }
        }

        notificationManager.notify(notificationId, builder.build())

        if (++notificationId >= Int.MAX_VALUE) {
            notificationId = 1
        }
        preferences.notificationId = notificationId
    }

    private fun createInboxStyle(
        title: String,
        contentList: List<NotificationContent>
    ): NotificationCompat.InboxStyle {
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)

        contentList.take(INBOX_LINE_LIMIT).forEach { content ->
            inboxStyle.addLine(content.styleText)
        }

        val moreContentSize = contentList.size - INBOX_LINE_LIMIT
        if (moreContentSize > 0) {
            inboxStyle.setSummaryText(
                context.getString(
                    R.string.notification_summary_inbox_style,
                    moreContentSize
                )
            )
        }

        return inboxStyle
    }
}
