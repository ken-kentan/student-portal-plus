package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import jp.kentan.studentportalplus.data.PortalRepository
import javax.inject.Inject

/**
 * ViewModelFactory
 *
 * @see <a href="https://github.com/googlesamples/android-architecture/blob/dev-todo-mvvm-live-kotlin/">GitHub</a>
 */
class ViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    @Inject
    lateinit var portalRepository: PortalRepository

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(DashboardViewModel::class.java) ->
                        DashboardViewModel(portalRepository)
                    else ->
                        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T
}