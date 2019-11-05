package jp.kentan.studentportalplus.ui.lectureinformation

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.FragmentScoped
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class LectureInformationModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeLectureInformationFragment(): LectureInformationFragment

    @Binds
    @IntoMap
    @ViewModelKey(LectureInformationViewModel::class)
    abstract fun bindLectureInformationViewModel(viewModel: LectureInformationViewModel): ViewModel

}