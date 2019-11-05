package jp.kentan.studentportalplus.di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import jp.kentan.studentportalplus.ui.ViewModelFactory

@Module
@Suppress("UNUSED")
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}