package jp.kentan.studentportalplus.ui.welcome.notification

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.FragmentScoped
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class WelcomeNotificationModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeWelcomeNotificationFragment(): WelcomeNotificationFragment

    @Binds
    @IntoMap
    @ViewModelKey(WelcomeNotificationViewModel::class)
    abstract fun bindWelcomeNotificationViewModel(viewModel: WelcomeNotificationViewModel): ViewModel
}
