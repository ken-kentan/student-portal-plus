package jp.kentan.studentportalplus.di

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.room.Room
import dagger.Module
import dagger.Provides
import jp.kentan.studentportalplus.StudentPortalPlus
import jp.kentan.studentportalplus.data.*
import jp.kentan.studentportalplus.data.dao.PortalDatabase
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.source.ShibbolethDataSource
import jp.kentan.studentportalplus.notification.NotificationHelper
import jp.kentan.studentportalplus.notification.SummaryNotification
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
        client: ShibbolethClient,
        source: ShibbolethDataSource
    ): UserRepository = DefaultUserRepository(client, source)

    @Provides
    @Singleton
    fun provideLectureInformationRepository(
        database: PortalDatabase,
        shibbolethClient: ShibbolethClient,
        localPreferences: LocalPreferences
    ): LectureInformationRepository = DefaultLectureInformationRepository(
        database.lectureInformationDao,
        shibbolethClient,
        database.attendCourseDao,
        localPreferences
    )

    @Provides
    @Singleton
    fun provideLectureCancellationRepository(
        database: PortalDatabase,
        shibbolethClient: ShibbolethClient,
        localPreferences: LocalPreferences
    ): LectureCancellationRepository = DefaultLectureCancellationRepository(
        database.lectureCancellationDao,
        shibbolethClient,
        database.attendCourseDao,
        localPreferences
    )

    @Provides
    @Singleton
    fun provideNoticeRepository(
        database: PortalDatabase,
        shibbolethClient: ShibbolethClient
    ): NoticeRepository = DefaultNoticeRepository(database.noticeDao, shibbolethClient)

    @Provides
    @Singleton
    fun provideAttendCourseRepository(
        database: PortalDatabase,
        shibbolethClient: ShibbolethClient
    ): AttendCourseRepository =
        DefaultAttendCourseRepository(database.attendCourseDao, shibbolethClient)

    @Provides
    fun provideNotificationHelper(
        context: Context,
        localPreferences: LocalPreferences
    ): NotificationHelper = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SummaryNotification(context, localPreferences)
    } else {
        TODO("VERSION.SDK_INT < N")
    }

}
