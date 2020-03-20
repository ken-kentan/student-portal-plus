package jp.kentan.studentportalplus.di

import androidx.work.WorkerFactory
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import jp.kentan.studentportalplus.work.ListenableWorkerFactory
import jp.kentan.studentportalplus.work.sync.SyncWorkerModule

@Module(
    includes = [
        AssistedInject_WorkerModule::class,
        SyncWorkerModule::class
    ]
)
@AssistedModule
@Suppress("UNUSED")
abstract class WorkerModule {

    @Binds
    abstract fun bindWorkerFactory(factory: ListenableWorkerFactory): WorkerFactory
}
