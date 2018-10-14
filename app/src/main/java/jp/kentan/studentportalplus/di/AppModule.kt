package jp.kentan.studentportalplus.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider
import jp.kentan.studentportalplus.ui.ViewModelFactory
import javax.inject.Singleton

@Module
class AppModule(
        private val app: Application
) {

    private val shibbolethDataProvider = ShibbolethDataProvider(app)
    private val portalRepository = PortalRepository(app, shibbolethDataProvider)
    private val userRepository = UserRepository(shibbolethDataProvider)

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun providePortalRepository(): PortalRepository = portalRepository

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository = userRepository

    @Provides
    @Singleton
    fun provideViewModelFactory() = ViewModelFactory(app, portalRepository, userRepository, shibbolethDataProvider)
}