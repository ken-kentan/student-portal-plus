package jp.kentan.studentportalplus.ui.lectures.cancellation

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.FragmentScoped
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class LectureCancellationsModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeLectureCancellationsFragment(): LectureCancellationsFragment

    @Binds
    @IntoMap
    @ViewModelKey(LectureCancellationsViewModel::class)
    abstract fun bindLectureCancellationsViewModel(viewModel: LectureCancellationsViewModel): ViewModel
}
