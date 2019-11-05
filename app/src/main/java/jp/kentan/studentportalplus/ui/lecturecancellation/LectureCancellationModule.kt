package jp.kentan.studentportalplus.ui.lecturecancellation

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.FragmentScoped
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class LectureCancellationModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeLectureCancellationFragment(): LectureCancellationFragment

    @Binds
    @IntoMap
    @ViewModelKey(LectureCancellationViewModel::class)
    abstract fun bindLectureCancellationViewModel(viewModel: LectureCancellationViewModel): ViewModel

}