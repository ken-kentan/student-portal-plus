package jp.kentan.studentportalplus.ui.timetable

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.FragmentScoped
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class TimetableModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeTimetableFragment(): TimetableFragment

    @Binds
    @IntoMap
    @ViewModelKey(TimetableViewModel::class)
    abstract fun bindTimetableViewModel(viewModel: TimetableViewModel): ViewModel
}
