package jp.kentan.studentportalplus.ui.lecturecancellationdetail

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class LectureCancellationDetailModule {

    @Binds
    @IntoMap
    @ViewModelKey(LectureCancellationDetailViewModel::class)
    abstract fun bindLectureCancellationDetailViewModel(viewModel: LectureCancellationDetailViewModel): ViewModel
}
