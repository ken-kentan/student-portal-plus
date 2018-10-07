package jp.kentan.studentportalplus.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.ui.lecturecancel.detail.LectureCancelDetailActivity
import jp.kentan.studentportalplus.ui.lectureinfo.detail.LectureInfoDetailActivity
import jp.kentan.studentportalplus.ui.login.LoginActivity
import jp.kentan.studentportalplus.ui.main.MainActivity
import jp.kentan.studentportalplus.ui.myclass.detail.MyClassDetailActivity
import jp.kentan.studentportalplus.ui.myclass.edit.MyClassEditActivity
import jp.kentan.studentportalplus.ui.notice.detail.NoticeDetailActivity

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    abstract fun contributeLectureInfoDetailActivity(): LectureInfoDetailActivity

    @ContributesAndroidInjector
    abstract fun contributeLectureCancelDetailActivity(): LectureCancelDetailActivity

    @ContributesAndroidInjector
    abstract fun contributeMyClassDetailActivity(): MyClassDetailActivity

    @ContributesAndroidInjector
    abstract fun contributeMyClassEditActivity(): MyClassEditActivity

    @ContributesAndroidInjector
    abstract fun contributeNoticeDetailActivity(): NoticeDetailActivity

}