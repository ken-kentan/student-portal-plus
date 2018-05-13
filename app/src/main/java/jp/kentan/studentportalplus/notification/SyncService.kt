package jp.kentan.studentportalplus.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.data.PortalRepository
import javax.inject.Inject

class SyncService : Service() {

    @Inject
    lateinit var repository: PortalRepository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AndroidInjection.inject(this)

        BackgroundSyncTask(applicationContext, repository).run { stopSelf() }

        return Service.START_NOT_STICKY
    }
}