package jp.kentan.studentportalplus.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import javax.inject.Singleton


@Module
class AppModule(private val app: Application) {

    private val portalRepository = PortalRepository(app.applicationContext)

    @Provides
    @Singleton
    fun provideContext(): Context = app.applicationContext

    @Provides
    @Singleton
    fun providePortalRepository() = portalRepository

    @Provides
    @Singleton
    fun provideViewModelFactory() = ViewModelFactory(portalRepository)

}