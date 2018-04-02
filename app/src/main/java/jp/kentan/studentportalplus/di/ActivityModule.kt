package jp.kentan.studentportalplus.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.ui.*

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeLectureInformationActivity(): LectureInformationActivity

    @ContributesAndroidInjector
    abstract fun contributeLectureCancellationActivity(): LectureCancellationActivity

    @ContributesAndroidInjector
    abstract fun contributeNoticeActivity(): NoticeActivity

    @ContributesAndroidInjector
    abstract fun contributeMyClassActivity(): MyClassActivity
}