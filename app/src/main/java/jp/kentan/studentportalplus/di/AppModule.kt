package jp.kentan.studentportalplus.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Singleton


@Module
class AppModule(app: Application) {

    private val context = app.applicationContext
    private val shibbolethDataProvider = ShibbolethDataProvider(context)
    private val portalRepository = PortalRepository(app.applicationContext, shibbolethDataProvider)

    @Provides
    @Singleton
    fun provideContext(): Context = context

    @Provides
    @Singleton
    fun provideShibbolethDataProvider() = shibbolethDataProvider

    @Provides
    @Singleton
    fun providePortalRepository() = portalRepository

    @Provides
    @Singleton
    fun provideViewModelFactory() = ViewModelFactory(context.defaultSharedPreferences, portalRepository)

}