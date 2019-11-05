package jp.kentan.studentportalplus.ui.editattendcourse

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class EditAttendCourseModule {

    @Binds
    @IntoMap
    @ViewModelKey(EditAttendCourseViewModel::class)
    abstract fun bindEditAttendCourseViewModel(viewModel: EditAttendCourseViewModel): ViewModel

}