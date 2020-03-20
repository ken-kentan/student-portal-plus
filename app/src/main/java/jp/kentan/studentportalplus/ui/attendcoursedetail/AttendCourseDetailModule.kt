package jp.kentan.studentportalplus.ui.attendcoursedetail

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class AttendCourseDetailModule {

    @Binds
    @IntoMap
    @ViewModelKey(AttendCourseDetailViewModel::class)
    abstract fun bindAttendCourseDetailViewModel(viewModel: AttendCourseDetailViewModel): ViewModel
}
