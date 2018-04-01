package jp.kentan.studentportalplus.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.ui.fragment.*

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeDashboardFragment(): DashboardFragment

    @ContributesAndroidInjector
    abstract fun contributeTimetableFragment(): TimetableFragment

    @ContributesAndroidInjector
    abstract fun contributeLectureInformationFragment(): LectureInformationFragment

    @ContributesAndroidInjector
    abstract fun contributeLectureCancellationFragment(): LectureCancellationFragment

    @ContributesAndroidInjector
    abstract fun contributeNoticeFragment(): NoticeFragment
}