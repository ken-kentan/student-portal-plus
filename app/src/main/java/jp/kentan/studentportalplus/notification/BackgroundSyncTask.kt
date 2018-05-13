package jp.kentan.studentportalplus.notification

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.NotifyContent
import jp.kentan.studentportalplus.data.component.NotifyType
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.data.component.PortalDataType.*
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import jp.kentan.studentportalplus.util.enableDetailErrorMessage
import jp.kentan.studentportalplus.util.getMyClassThreshold
import org.jetbrains.anko.defaultSharedPreferences
import java.util.*

class BackgroundSyncTask(
        private val context: Context,
        private val repository: PortalRepository
) {

    companion object {
        private val STRING_DISTANCE = JaroWinklerDistance()
    }

    private val preferences = context.defaultSharedPreferences

    fun run(onFinished: () -> Unit = {}) {
        if (isInMidnight()) {
            onFinished()
            return
        }

        val notification = NotificationController(context)
        notification.cancelErrorNotification()

        // Sync
        try {
            val newDataMap  = repository.sync()
            val subjectList = repository.getMyClassSubjectList()

            val threshold = preferences.getMyClassThreshold()

            val lectureInfoList   = newDataMap.getBy(LECTURE_INFORMATION, subjectList, threshold)
            val lectureCancelList = newDataMap.getBy(LECTURE_CANCELLATION, subjectList, threshold)
            val noticeList        = newDataMap.getBy(NOTICE)

            notification.notify(LECTURE_INFORMATION, lectureInfoList)
            notification.notify(LECTURE_CANCELLATION, lectureCancelList)
            notification.notify(NOTICE, noticeList)

            saveLastSyncTime()
        } catch (e: ShibbolethAuthenticationException) {
            notification.notifyError(e.message, true)
        } catch (e: Exception) {
            Log.e("BackgroundSyncTask", "Failed to sync", e)

            if (preferences.enableDetailErrorMessage()) {
                notification.notifyError(e.message ?: context.getString(R.string.error_unknown))
            }
        }

        onFinished()
    }

    private fun isInMidnight(): Boolean {
        return Calendar.getInstance(Locale.JAPAN).get(Calendar.HOUR_OF_DAY) !in 5..22
    }

    private fun saveLastSyncTime() {
        preferences.edit {
            putLong("last_sync_time_millis", System.currentTimeMillis())
        }
    }

    private fun Map<PortalDataType, List<NotifyContent>>.getBy(type: PortalDataType, subjects: List<String> = emptyList(), threshold: Float = 0f): List<NotifyContent> {
        val list = this[type] ?: return emptyList()

        return when (NotifyType.getBy(preferences, type.notifyTypeKey)) {
            NotifyType.ALL -> list
            NotifyType.ATTEND -> {
                list.filter {
                    val subject = it.title

                    subjects.forEach {
                        return@filter (it == subject || STRING_DISTANCE.getDistance(it, subject) >= threshold)
                    }

                    return@filter false
                }
            }
            NotifyType.NOT -> emptyList()
        }
    }
}