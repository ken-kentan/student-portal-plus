package jp.kentan.studentportalplus

import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import jp.kentan.studentportalplus.di.DaggerAppComponent
import javax.inject.Inject

class StudentPortalPlus : DaggerApplication() {

    @Inject
    lateinit var workerFactory: WorkerFactory

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent
            .factory()
            .create(this)
    }

    override fun onCreate() {
        super.onCreate()

        WorkManager.initialize(
            this,
            Configuration.Builder().setWorkerFactory(workerFactory).build()
        )
    }
}
