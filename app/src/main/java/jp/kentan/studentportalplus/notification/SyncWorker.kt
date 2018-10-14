package jp.kentan.studentportalplus.notification

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import jp.kentan.studentportalplus.StudentPortalPlus
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.PortalContent
import jp.kentan.studentportalplus.data.component.PortalData
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import jp.kentan.studentportalplus.util.getNotificationType
import jp.kentan.studentportalplus.util.getSimilarSubjectThresholdFloat
import jp.kentan.studentportalplus.util.isEnabledDetailError
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.defaultSharedPreferences
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import javax.inject.Inject

class SyncWorker(context : Context, params : WorkerParameters) : Worker(context, params) {

    companion object {
        const val NAME = "sync_worker"
        const val IGNORE_MIDNIGHT = "ignore_midnight"

        private const val TAG = "SyncWorker"
    }

    @Inject
    lateinit var repository: PortalRepository

    private val preferences = context.defaultSharedPreferences
    private val stringDistance = JaroWinklerDistance()

    override fun doWork(): Result {
        if (isInMidnight() && !inputData.getBoolean(IGNORE_MIDNIGHT, false)) {
            Log.d(TAG, "Skipped because of midnight")
            return Result.SUCCESS
        }

        (applicationContext as StudentPortalPlus).component.inject(this)

        val controller = NotificationController(applicationContext)
        controller.cancelErrorNotification()

        // Sync
        try {
            val updatedContentsMap = runBlocking { repository.sync().await() }
            val subjectList = repository.getMyClassSubjectList()

            val threshold = preferences.getSimilarSubjectThresholdFloat()

            val lectureInfoList = updatedContentsMap.getBy(PortalData.LECTURE_INFO, subjectList, threshold)
            val lectureCancelList = updatedContentsMap.getBy(PortalData.LECTURE_CANCEL, subjectList, threshold)
            val noticeList = updatedContentsMap.getBy(PortalData.NOTICE)

            controller.apply {
                notify(PortalData.LECTURE_INFO, lectureInfoList)
                notify(PortalData.LECTURE_CANCEL, lectureCancelList)
                notify(PortalData.NOTICE, noticeList)
            }
        } catch (e: ShibbolethAuthenticationException) {
            controller.notifyError(e.message, true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync", e)

            if (preferences.isEnabledDetailError()) {
                controller.notifyError(e.stackTraceToString())
            }
        }

        return Result.SUCCESS
    }

    private fun isInMidnight(): Boolean {
        val timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        return Calendar.getInstance(timeZone).get(Calendar.HOUR_OF_DAY) !in 5..22
    }

    private fun Map<PortalData, List<PortalContent>>.getBy(type: PortalData, subjects: List<String> = emptyList(), threshold: Float = 0f): List<PortalContent> {
        val list = this[type] ?: return emptyList()

        return when (preferences.getNotificationType(type)) {
            NotificationType.ALL -> list
            NotificationType.ATTEND -> {
                list.filter { content ->
                    val subject = content.title
                    return@filter subjects.any { it == subject || stringDistance.getDistance(it, subject) >= threshold }
                }
            }
            NotificationType.NOT -> emptyList()
        }
    }

    private fun Exception.stackTraceToString(): String {
        val sw = StringWriter()
        printStackTrace(PrintWriter(sw))

        return sw.toString()
    }
}