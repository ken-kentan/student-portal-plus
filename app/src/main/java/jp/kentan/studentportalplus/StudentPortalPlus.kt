package jp.kentan.studentportalplus


import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import jp.kentan.studentportalplus.di.ActivityModule
import jp.kentan.studentportalplus.di.AppModule
import jp.kentan.studentportalplus.di.FragmentModule
import jp.kentan.studentportalplus.di.ServiceModule
import javax.inject.Singleton


open class StudentPortalPlus : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<StudentPortalPlus> {
        return DaggerStudentPortalPlus_Component.builder()
                .appModule(AppModule(this))
                .build()
    }

    @Singleton
    @dagger.Component(modules = [
        (AndroidSupportInjectionModule::class),
        (AppModule::class),
        (ActivityModule::class),
        (FragmentModule::class),
        (ServiceModule::class)])
    internal interface Component : AndroidInjector<StudentPortalPlus>
}