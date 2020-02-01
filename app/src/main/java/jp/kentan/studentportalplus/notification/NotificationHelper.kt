package jp.kentan.studentportalplus.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.ui.MainActivity
import jp.kentan.studentportalplus.ui.login.LoginActivity

abstract class NotificationHelper(
    protected val context: Context
) {
    companion object {
        val VIBRATION_PATTERN = longArrayOf(0, 300, 300, 300)

        const val NEWLY_CHANNEL_ID = "0_newly_channel" // 新着通知
        const val APP_CHANNEL_ID = "99_app_channel"

        private const val ERROR_NOTIFICATION_ID = -1
    }

    protected val notificationManager = NotificationManagerCompat.from(context)

    abstract fun sendLectureInformation(lectureInfoList: List<LectureInformation>)

    abstract fun sendLectureCancellation(lectureCancelList: List<LectureCancellation>)

    abstract fun sendNotice(noticeList: List<Notice>)

    fun sendAuthenticationError(message: String) {
        sendErrorInternal(
            intent = LoginActivity.createIntent(context, true),
            subText = context.getString(R.string.title_required_login),
            title = context.getString(R.string.error),
            text = message
        )
    }

    fun sendError(throwable: Throwable) {
        sendErrorInternal(
            intent = MainActivity.createIntent(context),
            subText = context.getString(R.string.title_sync_failed),
            title = context.getString(R.string.error),
            text = throwable.message ?: throwable::class.java.simpleName
        )
    }

    private fun sendErrorInternal(
        intent: Intent,
        subText: String,
        title: String,
        text: String
    ) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            ERROR_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, APP_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setColor(ContextCompat.getColor(context, R.color.red_600))
//            .setSmallIcon(SMALL_APP_ICON)
            .setSubText(subText)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // TODO add retry service

        notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build())
    }
}
