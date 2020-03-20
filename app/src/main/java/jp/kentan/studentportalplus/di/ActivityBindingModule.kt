package jp.kentan.studentportalplus.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.kentan.studentportalplus.ui.MainActivity
import jp.kentan.studentportalplus.ui.MainModule
import jp.kentan.studentportalplus.ui.attendcoursedetail.AttendCourseDetailActivity
import jp.kentan.studentportalplus.ui.attendcoursedetail.AttendCourseDetailModule
import jp.kentan.studentportalplus.ui.dashboard.DashboardModule
import jp.kentan.studentportalplus.ui.editattendcourse.EditAttendCourseActivity
import jp.kentan.studentportalplus.ui.editattendcourse.EditAttendCourseModule
import jp.kentan.studentportalplus.ui.lecturecancellationdetail.LectureCancellationDetailActivity
import jp.kentan.studentportalplus.ui.lecturecancellationdetail.LectureCancellationDetailModule
import jp.kentan.studentportalplus.ui.lectureinformationdetail.LectureInformationDetailActivity
import jp.kentan.studentportalplus.ui.lectureinformationdetail.LectureInformationDetailModule
import jp.kentan.studentportalplus.ui.lectures.cancellation.LectureCancellationsModule
import jp.kentan.studentportalplus.ui.lectures.information.LectureInformationsModule
import jp.kentan.studentportalplus.ui.login.LoginModule
import jp.kentan.studentportalplus.ui.noticedetail.NoticeDetailActivity
import jp.kentan.studentportalplus.ui.noticedetail.NoticeDetailModule
import jp.kentan.studentportalplus.ui.notices.NoticesModule
import jp.kentan.studentportalplus.ui.settings.SettingsActivity
import jp.kentan.studentportalplus.ui.settings.SettingsModule
import jp.kentan.studentportalplus.ui.timetable.TimetableModule
import jp.kentan.studentportalplus.ui.welcome.WelcomeActivity
import jp.kentan.studentportalplus.ui.welcome.notification.WelcomeNotificationModule

@Module
@Suppress("UNUSED")
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            // activity
            MainModule::class,
            // fragments
            DashboardModule::class,
            TimetableModule::class,
            LectureInformationsModule::class,
            LectureCancellationsModule::class,
            NoticesModule::class
        ]
    )
    abstract fun contributeMainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [LectureInformationDetailModule::class])
    abstract fun contributeLectureInformationDetailActivity(): LectureInformationDetailActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [LectureCancellationDetailModule::class])
    abstract fun contributeLectureCancellationDetailActivity(): LectureCancellationDetailActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [AttendCourseDetailModule::class])
    abstract fun contributeAttendCourseDetailActivity(): AttendCourseDetailActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [NoticeDetailModule::class])
    abstract fun contributeNoticeDetailActivity(): NoticeDetailActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [EditAttendCourseModule::class])
    abstract fun contributeAttendCourseEditActivity(): EditAttendCourseActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [WelcomeNotificationModule::class, LoginModule::class])
    abstract fun contributeWelcomeActivity(): WelcomeActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [SettingsModule::class, LoginModule::class])
    abstract fun contributeSettingsActivity(): SettingsActivity
}
