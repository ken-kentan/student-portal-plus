package jp.kentan.studentportalplus.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice
import jp.kentan.studentportalplus.ui.MainActivity
import jp.kentan.studentportalplus.ui.settings.SettingsActivity
import jp.kentan.studentportalplus.work.sync.RetrySyncService

abstract class NotificationHelper(
    protected val context: Context
) {
    companion object {
        val VIBRATION_PATTERN = longArrayOf(0, 300, 300, 300)
        const val NOTIFICATION_LED_ON_MILLIS = 1000
        const val NOTIFICATION_LED_OFF_MILLIS = 2000

        const val NEWLY_CHANNEL_ID = "0_newly_channel" // 新着通知
        const val APP_CHANNEL_ID = "99_app_channel"

        private const val ERROR_NOTIFICATION_ID = -1
    }

    protected val notificationManager = NotificationManagerCompat.from(context)

    abstract fun sendLectureInformation(lectureInfoList: List<LectureInformation>)

    abstract fun sendLectureCancellation(lectureCancelList: List<LectureCancellation>)

    abstract fun sendNotice(noticeList: List<Notice>)

    fun sendShibbolethError(message: String) {
        sendErrorInternal(
            intent = SettingsActivity.createIntent(context),
            subText = context.getString(R.string.notification_required_login_again),
            title = context.getString(R.string.notification_error),
            text = message
        )
    }

    fun sendError(throwable: Throwable) {
        sendErrorInternal(
            intent = MainActivity.createIntent(context),
            subText = context.getString(R.string.notification_sync_failed),
            title = context.getString(R.string.notification_error),
            text = throwable.message ?: throwable::class.java.simpleName
        )
    }

    fun cancelError() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID)
    }

    private fun sendErrorInternal(intent: Intent, subText: String, title: String, text: String) {
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
            .setColor(ContextCompat.getColor(context, R.color.notification_error))
            .setSmallIcon(R.drawable.notification_app)
            .setSubText(subText)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val retrySyncService = PendingIntent.getService(
            context,
            ERROR_NOTIFICATION_ID,
            RetrySyncService.createIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val action = NotificationCompat.Action.Builder(
            R.drawable.notification_retry,
            context.getString(R.string.all_retry),
            retrySyncService
        ).build()

        builder.addAction(action)

        notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build())
    }
}
