package jp.kentan.studentportalplus.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.ui.dashboard.DashboardFragment
import jp.kentan.studentportalplus.ui.lecturecancel.LectureCancelFragment
import jp.kentan.studentportalplus.ui.lectureinfo.LectureInfoFragment
import jp.kentan.studentportalplus.ui.notice.NoticeFragment
import jp.kentan.studentportalplus.ui.setting.GeneralPreferenceFragment
import jp.kentan.studentportalplus.ui.timetable.TimetableFragment

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeDashboardFragment(): DashboardFragment

    @ContributesAndroidInjector
    abstract fun contributeTimetableFragment(): TimetableFragment

    @ContributesAndroidInjector
    abstract fun contributeLectureInfoFragment(): LectureInfoFragment

    @ContributesAndroidInjector
    abstract fun contributeLectureCancelFragment(): LectureCancelFragment

    @ContributesAndroidInjector
    abstract fun contributeNoticeFragment(): NoticeFragment

    @ContributesAndroidInjector
    abstract fun contributeGeneralPreferenceFragment(): GeneralPreferenceFragment

}