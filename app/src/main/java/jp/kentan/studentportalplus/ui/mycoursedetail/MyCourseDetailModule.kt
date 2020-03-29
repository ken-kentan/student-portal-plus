package jp.kentan.studentportalplus.ui.mycoursedetail

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class MyCourseDetailModule {

    @Binds
    @IntoMap
    @ViewModelKey(MyCourseDetailViewModel::class)
    abstract fun bindMyCourseDetailViewModel(viewModel: MyCourseDetailViewModel): ViewModel
}
