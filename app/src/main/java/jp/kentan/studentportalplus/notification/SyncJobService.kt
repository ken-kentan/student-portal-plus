package jp.kentan.studentportalplus.notification

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.data.PortalRepository
import org.jetbrains.anko.defaultSharedPreferences
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SyncJobService : JobService() {

    companion object {
        private const val TAG = "SyncJobService"
        const val ID = 1
    }

    @Inject
    lateinit var repository: PortalRepository

    override fun onStartJob(params: JobParameters): Boolean {
        val diffMillis = System.currentTimeMillis() - defaultSharedPreferences.getLong("scheduled_time_millis", 0)

        if (TimeUnit.MILLISECONDS.toMinutes(diffMillis) <= 5L) {
            Log.d(TAG, "Skipped sync because scheduled time too close")
            jobFinished(params, false)
            return true
        }

        AndroidInjection.inject(this)

        BackgroundSyncTask(applicationContext, repository).run {
            jobFinished(params, false)
        }

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = false
}