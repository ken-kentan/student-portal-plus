package jp.kentan.studentportalplus


import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import jp.kentan.studentportalplus.di.ActivityModule
import jp.kentan.studentportalplus.di.AppModule
import jp.kentan.studentportalplus.di.FragmentModule
import jp.kentan.studentportalplus.notification.NotificationController
import jp.kentan.studentportalplus.notification.SyncWorker
import javax.inject.Singleton


open class StudentPortalPlus : DaggerApplication() {

    val component: StudentPortalPlus.Component by lazy(LazyThreadSafetyMode.NONE) {
        DaggerStudentPortalPlus_Component.builder()
                .appModule(AppModule(this))
                .build()
    }

    override fun applicationInjector(): AndroidInjector<StudentPortalPlus> {
        return component
    }

    override fun onCreate() {
        super.onCreate()

        NotificationController.setupChannel(this)
    }

    @Singleton
    @dagger.Component(modules = [
        (AndroidSupportInjectionModule::class),
        (AppModule::class),
        (ActivityModule::class),
        (FragmentModule::class)])
    interface Component : AndroidInjector<StudentPortalPlus> {
        fun inject(syncWorker: SyncWorker)
    }
}