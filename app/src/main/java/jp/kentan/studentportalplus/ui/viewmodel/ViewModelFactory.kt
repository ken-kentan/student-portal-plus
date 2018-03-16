package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import jp.kentan.studentportalplus.data.PortalRepository

/**
 * ViewModelFactory
 *
 * @see <a href="https://github.com/googlesamples/android-architecture/blob/dev-todo-mvvm-live-kotlin/">GitHub</a>
 */
class ViewModelFactory(
        private val portalRepository: PortalRepository
) : ViewModelProvider.NewInstanceFactory() {

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

    companion object {
//        @SuppressLint("StaticFieldLeak")
//        val instance = ViewModelFactory(Injection.provide)

//        fun getInstance(application: Application) =
//                INSTANCE ?: synchronized(ViewModelFactory::class.java) {
//                    INSTANCE ?: ViewModelFactory(application,
//                            Injection.provideTasksRepository(application.applicationContext))
//                            .also { INSTANCE = it }
//                }
//
//
//        @VisibleForTesting fun destroyInstance() {
//            INSTANCE = null
//        }
    }
}