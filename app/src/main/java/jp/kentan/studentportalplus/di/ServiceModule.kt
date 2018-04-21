package jp.kentan.studentportalplus.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.notification.SyncJobService

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun contributeSyncJobService(): SyncJobService
}