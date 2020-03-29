package jp.kentan.studentportalplus.di

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.room.Room
import dagger.Module
import dagger.Provides
import jp.kentan.studentportalplus.StudentPortalPlus
import jp.kentan.studentportalplus.data.DefaultLectureCancellationRepository
import jp.kentan.studentportalplus.data.DefaultLectureInformationRepository
import jp.kentan.studentportalplus.data.DefaultMyCourseRepository
import jp.kentan.studentportalplus.data.DefaultNoticeRepository
import jp.kentan.studentportalplus.data.DefaultSubjectRepository
import jp.kentan.studentportalplus.data.DefaultUserRepository
import jp.kentan.studentportalplus.data.LectureCancellationRepository
import jp.kentan.studentportalplus.data.LectureInformationRepository
import jp.kentan.studentportalplus.data.LocalPreferences
import jp.kentan.studentportalplus.data.MyCourseRepository
import jp.kentan.studentportalplus.data.NoticeRepository
import jp.kentan.studentportalplus.data.Preferences
import jp.kentan.studentportalplus.data.SubjectRepository
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.data.dao.PortalDatabase
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.source.ShibbolethDataSource
import jp.kentan.studentportalplus.notification.InboxStyleNotificationHelper
import jp.kentan.studentportalplus.notification.NotificationHelper
import jp.kentan.studentportalplus.notification.SummaryNotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
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
    fun provideShibbolethClient(source: ShibbolethDataSource, preferences: Preferences) =
        ShibbolethClient(source, preferences)

    @Provides
    @Singleton
    fun providePreferences(context: Context): Preferences = LocalPreferences(context)

    @Provides
    @Singleton
    fun provideUserRepository(
        source: ShibbolethDataSource
    ): UserRepository = DefaultUserRepository(source)

    @Provides
    @Singleton
    fun provideLectureInformationRepository(
        database: PortalDatabase,
        preferences: Preferences
    ): LectureInformationRepository = DefaultLectureInformationRepository(
        database.lectureInformationDao,
        database.myCourseDao,
        preferences
    )

    @Provides
    @Singleton
    fun provideLectureCancellationRepository(
        database: PortalDatabase,
        preferences: Preferences
    ): LectureCancellationRepository = DefaultLectureCancellationRepository(
        database.lectureCancellationDao,
        database.myCourseDao,
        preferences
    )

    @Provides
    @Singleton
    fun provideNoticeRepository(
        database: PortalDatabase
    ): NoticeRepository = DefaultNoticeRepository(database.noticeDao)

    @Provides
    @Singleton
    fun provideMyCourseRepository(
        database: PortalDatabase
    ): MyCourseRepository =
        DefaultMyCourseRepository(database.myCourseDao)

    @Provides
    @Singleton
    fun provideSubjectRepository(
        database: PortalDatabase
    ): SubjectRepository = DefaultSubjectRepository(
        database.lectureInformationDao,
        database.lectureCancellationDao,
        database.myCourseDao
    )

    @Provides
    fun provideNotificationHelper(
        context: Context,
        preferences: Preferences
    ): NotificationHelper = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SummaryNotificationHelper(context, preferences)
    } else {
        InboxStyleNotificationHelper(context, preferences)
    }
}
