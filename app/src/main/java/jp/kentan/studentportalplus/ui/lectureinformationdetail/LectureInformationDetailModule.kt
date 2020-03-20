package jp.kentan.studentportalplus.ui.lectureinformationdetail

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class LectureInformationDetailModule {

    @Binds
    @IntoMap
    @ViewModelKey(LectureInformationDetailViewModel::class)
    abstract fun bindLectureInformationDetailViewModel(viewModel: LectureInformationDetailViewModel): ViewModel
}
