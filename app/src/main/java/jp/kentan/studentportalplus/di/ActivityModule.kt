package jp.kentan.studentportalplus.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.ui.MainActivity
import jp.kentan.studentportalplus.ui.NoticeActivity
import jp.kentan.studentportalplus.ui.fragment.DashboardFragment

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeNoticeActivity(): NoticeActivity
}