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
    fun provideNoticeRepository(database: PortalDatabase): NoticeRepository =
        DefaultNoticeRepository(database.noticeDao)

    @Provides
    @Singleton
    fun provideAttendCourseRepository(database: PortalDatabase): AttendCourseRepository =
        DefaultAttendCourseRepository(database.attendCourseDao)

}
