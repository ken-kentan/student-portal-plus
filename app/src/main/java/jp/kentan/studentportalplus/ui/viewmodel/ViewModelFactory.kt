package jp.kentan.studentportalplus.ui.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import jp.kentan.studentportalplus.data.PortalRepository


class ViewModelFactory(private val portalRepository: PortalRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(DashboardFragmentViewModel::class.java) ->
                        DashboardFragmentViewModel(portalRepository)
                    isAssignableFrom(LectureInformationFragmentViewModel::class.java) ->
                        LectureInformationFragmentViewModel(portalRepository)
                    isAssignableFrom(NoticeFragmentViewModel::class.java) ->
                        NoticeFragmentViewModel(portalRepository)
                    isAssignableFrom(LectureInformationViewModel::class.java) ->
                        LectureInformationViewModel(portalRepository)
                    isAssignableFrom(NoticeViewModel::class.java) ->
                        NoticeViewModel(portalRepository)
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T
}