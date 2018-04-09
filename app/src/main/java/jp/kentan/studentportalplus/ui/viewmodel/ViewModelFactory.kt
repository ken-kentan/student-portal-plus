package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.SharedPreferences
import jp.kentan.studentportalplus.data.PortalRepository


class ViewModelFactory(
        private val sharedPreferences: SharedPreferences,
        private val portalRepository: PortalRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(DashboardFragmentViewModel::class.java) ->
                        DashboardFragmentViewModel(portalRepository)

                    isAssignableFrom(TimetableFragmentViewModel::class.java) ->
                        TimetableFragmentViewModel(portalRepository)

                    isAssignableFrom(LectureInformationFragmentViewModel::class.java) ->
                        LectureInformationFragmentViewModel(sharedPreferences, portalRepository)

                    isAssignableFrom(LectureCancellationFragmentViewModel::class.java) ->
                        LectureCancellationFragmentViewModel(sharedPreferences, portalRepository)

                    isAssignableFrom(NoticeFragmentViewModel::class.java) ->
                        NoticeFragmentViewModel(portalRepository)

                    isAssignableFrom(LectureInformationViewModel::class.java) ->
                        LectureInformationViewModel(portalRepository)

                    isAssignableFrom(LectureCancellationViewModel::class.java) ->
                        LectureCancellationViewModel(portalRepository)

                    isAssignableFrom(NoticeViewModel::class.java) ->
                        NoticeViewModel(portalRepository)

                    isAssignableFrom(MyClassViewModel::class.java) ->
                        MyClassViewModel(portalRepository)

                    isAssignableFrom(MyClassEditViewModel::class.java) ->
                        MyClassEditViewModel(portalRepository)

                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T
}