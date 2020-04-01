package jp.kentan.studentportalplus.ui.editmycourse

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class EditMyCourseModule {

    @Binds
    @IntoMap
    @ViewModelKey(EditMyCourseViewModel::class)
    abstract fun bindEditMyCourseViewModel(viewModel: EditMyCourseViewModel): ViewModel
}
