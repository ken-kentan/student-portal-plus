package jp.kentan.studentportalplus.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.ui.LectureInformationActivity
import jp.kentan.studentportalplus.ui.MainActivity
import jp.kentan.studentportalplus.ui.NoticeActivity

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeLectureInformationActivity(): LectureInformationActivity

    @ContributesAndroidInjector
    abstract fun contributeNoticeActivity(): NoticeActivity
}