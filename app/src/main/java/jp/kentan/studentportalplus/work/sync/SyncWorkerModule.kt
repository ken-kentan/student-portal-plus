package jp.kentan.studentportalplus.work.sync

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.WorkerKey
import jp.kentan.studentportalplus.work.ChildWorkerFactory

@Module
@Suppress("UNUSED")
interface SyncWorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(SyncWorker::class)
    fun bindSyncWorker(factory: SyncWorker.Factory): ChildWorkerFactory
}
