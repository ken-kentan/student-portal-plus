package jp.kentan.studentportalplus.ui.notice

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.FragmentScoped
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class NoticeModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeNoticeFragment(): NoticeFragment

    @Binds
    @IntoMap
    @ViewModelKey(NoticeViewModel::class)
    abstract fun bindNoticeViewModel(viewModel: NoticeViewModel): ViewModel

}