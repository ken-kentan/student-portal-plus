package jp.kentan.studentportalplus.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import jp.kentan.studentportalplus.data.PortalRepository
import javax.inject.Singleton


@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = app.applicationContext

    @Provides
    @Singleton
    fun providePortalRepository() = PortalRepository(app.applicationContext)
}