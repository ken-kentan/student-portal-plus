package jp.kentan.studentportalplus.ui.lectures.information

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.FragmentScoped
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class LectureInformationsModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeLectureInformationsFragment(): LectureInformationsFragment

    @Binds
    @IntoMap
    @ViewModelKey(LectureInformationsViewModel::class)
    abstract fun bindLectureInformationsViewModel(viewModel: LectureInformationsViewModel): ViewModel

}
