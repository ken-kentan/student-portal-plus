package jp.kentan.studentportalplus.notification

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.firebase.jobdispatcher.SimpleJobService
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.component.NotifyContent
import jp.kentan.studentportalplus.data.component.NotifyType
import jp.kentan.studentportalplus.data.component.PortalDataType
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.util.JaroWinklerDistance
import jp.kentan.studentportalplus.util.getMyClassThreshold
import org.jetbrains.anko.defaultSharedPreferences
import java.util.*
import javax.inject.Inject

class SyncJobService : SimpleJobService() {

    companion object {
        const val TAG = "SyncJobService"

        private val STRING_DISTANCE = JaroWinklerDistance()
    }

    @Inject
    lateinit var repository: PortalRepository

    override fun onRunJob(job: JobParameters): Int {
        Log.d(TAG, "onRunJob")

        if (isMidnight() && job.extras?.getBoolean("ignore_midnight", false) == false) {
            return JobService.RESULT_SUCCESS
        }

        AndroidInjection.inject(this)

        doSync()

        return JobService.RESULT_SUCCESS
    }

    private fun doSync() {
        val notificationController = NotificationController(this)
        val pref = defaultSharedPreferences

        notificationController.cancelErrorNotification()

        try {
            val newDataMap  = repository.sync()
            val subjectList = repository.getMyClassSubjectList()

            val threshold = pref.getMyClassThreshold()

            val lectureInfoList   = newDataMap[PortalDataType.LECTURE_INFORMATION]
                    .filterBy(pref.getNotifyType("notify_type_lecture_info"), subjectList, threshold)
            val lectureCancelList = newDataMap[PortalDataType.LECTURE_CANCELLATION]
                    .filterBy(pref.getNotifyType("notify_type_lecture_cancel"), subjectList, threshold)
            val noticeList        = newDataMap[PortalDataType.NOTICE]
                    .filterBy(pref.getNotifyType("notify_type_notice"))

            notificationController.notify(PortalDataType.LECTURE_INFORMATION, lectureInfoList)
            notificationController.notify(PortalDataType.LECTURE_CANCELLATION, lectureCancelList)
            notificationController.notify(PortalDataType.NOTICE, noticeList)

            saveLastSyncTime()
        } catch (e: ShibbolethAuthenticationException) {
            notificationController.notifyError(e.message, true)
        } catch (e: Exception) {
            if (pref.getBoolean("enable_detail_error", false)) {
                notificationController.notifyError(e.message ?: getString(R.string.error_unknown))
            }
        }
    }

    private fun isMidnight(): Boolean {
        val hour = Calendar.getInstance(Locale.JAPAN).get(Calendar.HOUR_OF_DAY)

        return hour !in 5..22
    }

    private fun saveLastSyncTime() {
        defaultSharedPreferences.edit {
            putLong("last_sync_time_millis", System.currentTimeMillis())
        }
    }

    private fun SharedPreferences.getNotifyType(key: String): NotifyType {
        return NotifyType.valueOf(this.getString(key, NotifyType.ALL.name))
    }

    private fun List<NotifyContent>?.filterBy(type: NotifyType): List<NotifyContent>{
        return if (type != NotifyType.ALL || this == null) {
            emptyList()
        } else {
            this
        }
    }

    private fun List<NotifyContent>?.filterBy(type: NotifyType, subjects: List<String>, threshold: Float): List<NotifyContent> {
        if (this == null) {
            return emptyList()
        }

        return when (type) {
            NotifyType.ALL -> this
            NotifyType.ATTEND -> {
                filter {
                    val subject = it.title

                    subjects.forEach {
                        return@filter (it == subject || STRING_DISTANCE.getDistance(it, subject) >= threshold)
                    }

                    false
                }
            }
            NotifyType.NOT -> emptyList()
        }
    }
}