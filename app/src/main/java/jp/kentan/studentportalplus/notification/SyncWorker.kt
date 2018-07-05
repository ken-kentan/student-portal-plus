package jp.kentan.studentportalplus.notification

import android.util.Log
import androidx.work.Worker
import jp.kentan.studentportalplus.StudentPortalPlus
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.NotifyContent
import jp.kentan.studentportalplus.data.component.NotifyType
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.data.component.PortalDataType.*
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import jp.kentan.studentportalplus.util.enabledDetailError
import jp.kentan.studentportalplus.util.getMyClassThreshold
import org.jetbrains.anko.defaultSharedPreferences
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import javax.inject.Inject

class SyncWorker : Worker() {

    companion object {
        const val NAME = "sync_worker"

        const val IGNORE_MIDNIGHT = "ignore_midnight"

        private const val TAG = "SyncWorker"
        private val STRING_DISTANCE = JaroWinklerDistance()
    }

    @Inject
    lateinit var repository: PortalRepository

    private val preferences by lazy { applicationContext.defaultSharedPreferences }

    override fun doWork(): Result {
        if (isInMidnight() && !inputData.getBoolean(IGNORE_MIDNIGHT, false)) {
            Log.d(TAG, "Skipped because of midnight")
            return Result.SUCCESS
        }

        (applicationContext as StudentPortalPlus).component.inject(this)

        val notification = NotificationController(applicationContext)
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

        } catch (e: ShibbolethAuthenticationException) {
            notification.notifyError(e.message, true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync", e)

            if (preferences.enabledDetailError()) {
                notification.notifyError(e.stackTraceToString())
            }
        }

        return Result.SUCCESS
    }

    private fun isInMidnight(): Boolean {
        return Calendar.getInstance(Locale.JAPAN).get(Calendar.HOUR_OF_DAY) !in 5..22
    }

    private fun Map<PortalDataType, List<NotifyContent>>.getBy(type: PortalDataType, subjects: List<String> = emptyList(), threshold: Float = 0f): List<NotifyContent> {
        val list = this[type] ?: return emptyList()

        return when (NotifyType.getBy(preferences, type.notifyTypeKey)) {
            NotifyType.ALL -> list
            NotifyType.ATTEND -> {
                list.filter {
                    val subject = it.title
                    return@filter subjects.any { it == subject || STRING_DISTANCE.getDistance(it, subject) >= threshold }
                }
            }
            NotifyType.NOT -> emptyList()
        }
    }

    private fun Exception.stackTraceToString(): String {
        val sw = StringWriter()
        printStackTrace(PrintWriter(sw))

        return sw.toString()
    }
}