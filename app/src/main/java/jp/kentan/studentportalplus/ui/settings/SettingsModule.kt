package jp.kentan.studentportalplus.ui.settings

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.di.FragmentScoped

@Module
@Suppress("UNUSED")
abstract class SettingsModule {
    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeGeneralPreferenceFragment(): GeneralPreferenceFragment
}
