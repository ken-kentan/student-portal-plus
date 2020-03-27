package jp.kentan.studentportalplus.di

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.room.Room
import dagger.Module
import dagger.Provides
import jp.kentan.studentportalplus.StudentPortalPlus
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.DefaultAttendCourseRepository
import jp.kentan.studentportalplus.data.DefaultLectureCancellationRepository
import jp.kentan.studentportalplus.data.DefaultLectureInformationRepository
import jp.kentan.studentportalplus.data.DefaultNoticeRepository
import jp.kentan.studentportalplus.data.DefaultSubjectRepository
import jp.kentan.studentportalplus.data.DefaultUserRepository
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.data.SubjectRepository
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.data.dao.PortalDatabase
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.source.ShibbolethDataSource
import jp.kentan.studentportalplus.notification.InboxStyleNotificationHelper
import jp.kentan.studentportalplus.notification.NotificationHelper
import jp.kentan.studentportalplus.notification.SummaryNotificationHelper
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    fun provideApplication(application: StudentPortalPlus): Application = application

    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun providePortalDatabase(context: Context): PortalDatabase = Room.databaseBuilder(
        context.applicationContext,
        PortalDatabase::class.java,
        "portal_database"
    ).build()

    @Provides
    @Singleton
    fun provideShibbolethDataSource(context: Context) = ShibbolethDataSource(context)

    @Provides
    @Singleton
    fun provideShibbolethClient(source: ShibbolethDataSource, localPreferences: LocalPreferences) =
        ShibbolethClient(source, localPreferences)

    @Provides
    @Singleton
    fun provideLocalPreferences(context: Context) = LocalPreferences(context)

    @Provides
    @Singleton
    fun provideUserRepository(
        source: ShibbolethDataSource
    ): UserRepository = DefaultUserRepository(source)

    @Provides
    @Singleton
    fun provideLectureInformationRepository(
        database: PortalDatabase,
        localPreferences: LocalPreferences
    ): LectureInformationRepository = DefaultLectureInformationRepository(
        database.lectureInformationDao,
        database.attendCourseDao,
        localPreferences
    )

    @Provides
    @Singleton
    fun provideLectureCancellationRepository(
        database: PortalDatabase,
        localPreferences: LocalPreferences
    ): LectureCancellationRepository = DefaultLectureCancellationRepository(
        database.lectureCancellationDao,
        database.attendCourseDao,
        localPreferences
    )

    @Provides
    @Singleton
    fun provideNoticeRepository(
        database: PortalDatabase
    ): NoticeRepository = DefaultNoticeRepository(database.noticeDao)

    @Provides
    @Singleton
    fun provideAttendCourseRepository(
        database: PortalDatabase
    ): AttendCourseRepository =
        DefaultAttendCourseRepository(database.attendCourseDao)

    @Provides
    @Singleton
    fun provideSubjectRepository(
        database: PortalDatabase
    ): SubjectRepository = DefaultSubjectRepository(
        database.lectureInformationDao,
        database.lectureCancellationDao,
        database.attendCourseDao
    )

    @Provides
    fun provideNotificationHelper(
        context: Context,
        localPreferences: LocalPreferences
    ): NotificationHelper = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SummaryNotificationHelper(context, localPreferences)
    } else {
        InboxStyleNotificationHelper(context, localPreferences)
    }
}
