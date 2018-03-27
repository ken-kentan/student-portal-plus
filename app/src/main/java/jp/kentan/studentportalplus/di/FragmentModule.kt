package jp.kentan.studentportalplus.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.ui.fragment.DashboardFragment
import jp.kentan.studentportalplus.ui.fragment.LectureInformationFragment
import jp.kentan.studentportalplus.ui.fragment.NoticeFragment

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeDashboardFragment(): DashboardFragment

    @ContributesAndroidInjector
    abstract fun contributeLectureInformationFragment(): LectureInformationFragment

    @ContributesAndroidInjector
    abstract fun contributeNoticeFragment(): NoticeFragment
}