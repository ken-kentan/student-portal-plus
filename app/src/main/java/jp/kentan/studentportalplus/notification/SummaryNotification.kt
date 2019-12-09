package jp.kentan.studentportalplus.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.entity.LectureCancellation
import jp.kentan.studentportalplus.data.entity.LectureInformation
import jp.kentan.studentportalplus.data.entity.Notice

@RequiresApi(Build.VERSION_CODES.N)
class SummaryNotification(
    private val context: Context
) : NotificationHelper() {

    companion object {
        private const val NEWLY_CHANNEL_ID = "0_newly_channel" // 新着通知
        private const val APP_CHANNEL_ID = "99_app_channel"

        private const val GROUP_KEY = "student_portal_plus"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupChannels() {
        val colorLed = ContextCompat.getColor(context, R.color.notification_led)

        val newlyChannel = NotificationChannel(
            NEWLY_CHANNEL_ID,
            context.getString(R.string.name_newly_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableLights(true)
            lightColor = colorLed
            vibrationPattern = VIBRATION_PATTERN
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val appChannel = NotificationChannel(
            APP_CHANNEL_ID,
            context.getString(R.string.name_app_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            enableLights(true)
            lightColor = colorLed
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
