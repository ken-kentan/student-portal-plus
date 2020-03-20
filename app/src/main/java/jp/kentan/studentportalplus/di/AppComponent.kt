package jp.kentan.studentportalplus.di

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import jp.kentan.studentportalplus.StudentPortalPlus
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        ActivityBindingModule::class,
        ViewModelModule::class,
        WorkerModule::class
    ]
)
interface AppComponent : AndroidInjector<StudentPortalPlus> {

    @Component.Factory
    interface Factory : AndroidInjector.Factory<StudentPortalPlus>
}
