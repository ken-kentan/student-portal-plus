package jp.kentan.studentportalplus.ui.noticedetail

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class NoticeDetailModule {

    @Binds
    @IntoMap
    @ViewModelKey(NoticeDetailViewModel::class)
    abstract fun bindNoticeDetailViewModel(viewModel: NoticeDetailViewModel): ViewModel
}
