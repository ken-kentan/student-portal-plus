package jp.kentan.studentportalplus.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethDataProvider
import jp.kentan.studentportalplus.ui.dashboard.DashboardViewModel
import jp.kentan.studentportalplus.ui.lecturecancel.LectureCancelViewModel
import jp.kentan.studentportalplus.ui.lecturecancel.detail.LectureCancelDetailViewModel
import jp.kentan.studentportalplus.ui.lectureinfo.LectureInfoViewModel
import jp.kentan.studentportalplus.ui.lectureinfo.detail.LectureInfoDetailViewModel
import jp.kentan.studentportalplus.ui.login.LoginViewModel
import jp.kentan.studentportalplus.ui.main.MainViewModel
import jp.kentan.studentportalplus.ui.myclass.detail.MyClassDetailViewModel
import jp.kentan.studentportalplus.ui.myclass.edit.MyClassEditViewModel
import jp.kentan.studentportalplus.ui.notice.NoticeViewModel
import jp.kentan.studentportalplus.ui.notice.detail.NoticeDetailViewModel
import jp.kentan.studentportalplus.ui.timetable.TimetableViewModel
import org.jetbrains.anko.defaultSharedPreferences

class ViewModelFactory(
        private val context: Application,
        private val portalRepository: PortalRepository,
        private val userRepository: UserRepository,
        private val shibbolethDataProvider: ShibbolethDataProvider
) : ViewModelProvider.AndroidViewModelFactory(context) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) =
            with(modelClass) {
                when {
                    isAssignableFrom(MainViewModel::class.java) ->
                        MainViewModel(portalRepository, userRepository)
                    isAssignableFrom(DashboardViewModel::class.java) ->
                        DashboardViewModel(portalRepository)
                    isAssignableFrom(TimetableViewModel::class.java) ->
                        TimetableViewModel(context.defaultSharedPreferences, portalRepository)
                    isAssignableFrom(LectureInfoViewModel::class.java) ->
                        LectureInfoViewModel(context.defaultSharedPreferences, portalRepository)
                    isAssignableFrom(LectureCancelViewModel::class.java) ->
                        LectureCancelViewModel(context.defaultSharedPreferences, portalRepository)
                    isAssignableFrom(NoticeViewModel::class.java) ->
                        NoticeViewModel(portalRepository)
                    isAssignableFrom(LectureInfoDetailViewModel::class.java) ->
                        LectureInfoDetailViewModel(context, portalRepository)
                    isAssignableFrom(LectureCancelDetailViewModel::class.java) ->
                        LectureCancelDetailViewModel(context, portalRepository)
                    isAssignableFrom(NoticeDetailViewModel::class.java) ->
                        NoticeDetailViewModel(context, portalRepository)
                    isAssignableFrom(MyClassDetailViewModel::class.java) ->
                        MyClassDetailViewModel(portalRepository)
                    isAssignableFrom(MyClassEditViewModel::class.java) ->
                        MyClassEditViewModel(portalRepository)
                    isAssignableFrom(LoginViewModel::class.java) ->
                        LoginViewModel(context, shibbolethDataProvider)

                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T
}