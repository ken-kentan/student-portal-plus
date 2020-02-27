package jp.kentan.studentportalplus.ui.login

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.kentan.studentportalplus.di.FragmentScoped
import jp.kentan.studentportalplus.di.ViewModelKey

@Module
@Suppress("UNUSED")
abstract class LoginModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeLoginFragment(): LoginFragment

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(viewModel: LoginViewModel): ViewModel

}
