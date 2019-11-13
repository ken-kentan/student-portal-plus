package jp.kentan.studentportalplus.ui.notices

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.FragmentScoped
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class NoticesModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeNoticesFragment(): NoticesFragment

    @Binds
    @IntoMap
    @ViewModelKey(NoticesViewModel::class)
    abstract fun bindNoticesViewModel(viewModel: NoticesViewModel): ViewModel

}
