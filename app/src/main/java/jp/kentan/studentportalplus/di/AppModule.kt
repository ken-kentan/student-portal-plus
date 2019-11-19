package jp.kentan.studentportalplus.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import jp.kentan.studentportalplus.StudentPortalPlus
import jp.kentan.studentportalplus.data.*
import jp.kentan.studentportalplus.data.dao.PortalDatabase
import jp.kentan.studentportalplus.data.source.ShibbolethClient
import jp.kentan.studentportalplus.data.source.ShibbolethDataSource
import javax.inject.Singleton

@Module
object AppModule {

    @JvmStatic
    @Provides
    fun provideApplication(application: StudentPortalPlus): Application = application

    @JvmStatic
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    @JvmStatic
    @Provides
    @Singleton
    fun providePortalDatabase(context: Context): PortalDatabase = Room.databaseBuilder(
        context.applicationContext,
        PortalDatabase::class.java,
        "portal_database"
    ).build()

    @JvmStatic
    @Provides
    @Singleton
    fun provideShibbolethDataSource(context: Context) = ShibbolethDataSource(context)

    @JvmStatic
    @Provides
    @Singleton
    fun provideShibbolethClient(source: ShibbolethDataSource, localPreferences: LocalPreferences) =
        ShibbolethClient(source, localPreferences)

    @JvmStatic
    @Provides
    @Singleton
    fun provideLocalPreferences(context: Context) = LocalPreferences(context)

    @JvmStatic
    @Provides
    @Singleton
    fun provideUserRepository(client: ShibbolethClient, source: ShibbolethDataSource) =
        UserRepository(client, source)

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
    @Provides
    @Singleton
    fun provideNoticeRepository(database: PortalDatabase): NoticeRepository =
        DefaultNoticeRepository(database.noticeDao)

    @JvmStatic
    @Provides
    @Singleton
    fun provideAttendCourseRepository(database: PortalDatabase): AttendCourseRepository =
        DefaultAttendCourseRepository(database.attendCourseDao)

}
