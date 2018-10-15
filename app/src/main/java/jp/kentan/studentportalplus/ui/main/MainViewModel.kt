package jp.kentan.studentportalplus.ui.main

import android.util.Log
import android.view.MenuItem
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.data.UserRepository
import jp.kentan.studentportalplus.data.shibboleth.ShibbolethAuthenticationException
import jp.kentan.studentportalplus.ui.SingleLiveData
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch


class MainViewModel(
        private val portalRepository: PortalRepository,
        userRepository: UserRepository
) : ViewModel(), NavigationView.OnNavigationItemSelectedListener {

    val attachFragment = SingleLiveData<FragmentType>()
    val replaceFragment = SingleLiveData<FragmentType>()
    val startSettingsActivity = SingleLiveData<Unit>()

    val user = userRepository.getUser()
    val isSyncing = ObservableBoolean()
    val openMap = SingleLiveData<MapHelper.Type>()
    val closeDrawer = SingleLiveData<Unit>()
    val indefiniteSnackbar = SingleLiveData<Pair<String?, Boolean>>()
    val finishSnackbar = SingleLiveData<Snackbar.Callback>()

    private var currentFragment = FragmentType.DASHBOARD
    private var canFinish = false

    fun onActivityCreated(isSync: Boolean) {
        portalRepository.loadFromDb()

        if (isSync) {
            onRefresh()
        }
    }

    fun onRefresh() {
        isSyncing.set(true)

        GlobalScope.launch {
            try {
                portalRepository.sync().await()
            } catch (e: Exception) {
                indefiniteSnackbar.postValue(Pair(e.message, e is ShibbolethAuthenticationException))
                Log.e(javaClass.simpleName, "Failed to refresh", e)
            }
            isSyncing.set(false)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        closeDrawer.value = Unit

        GlobalScope.launch(Dispatchers.Main) {
            delay(300)

            when (item.itemId) {
                R.id.nav_dashboard -> submitFragmentIfNeeded(FragmentType.DASHBOARD)
                R.id.nav_timetable -> submitFragmentIfNeeded(FragmentType.TIMETABLE)
                R.id.nav_lecture_info -> submitFragmentIfNeeded(FragmentType.LECTURE_INFO)
                R.id.nav_lecture_cancel -> submitFragmentIfNeeded(FragmentType.LECTURE_CANCEL)
                R.id.nav_notice -> submitFragmentIfNeeded(FragmentType.NOTICE)
                R.id.nav_campus_map -> openMap.value = MapHelper.Type.CAMPUS
                R.id.nav_room_map -> openMap.value = MapHelper.Type.ROOM
                R.id.nav_setting -> startSettingsActivity.value = Unit
            }
        }

        return true
    }

    fun onLectureInfoButtonClick() {
        submitFragmentIfNeeded(FragmentType.LECTURE_INFO)
    }

    fun onLectureCancelButtonClick() {
        submitFragmentIfNeeded(FragmentType.LECTURE_CANCEL)
    }

    fun onNoticeButtonClick() {
        submitFragmentIfNeeded(FragmentType.NOTICE)
    }

    fun onAttachFragment(type: FragmentType) {
        currentFragment = type
        attachFragment.value = type
    }

    fun canFinish(): Boolean {
        if (!canFinish) {
            canFinish = true

            finishSnackbar.value = object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    canFinish = false
                    super.onDismissed(transientBottomBar, event)
                }
            }

            return false
        }

        return true
    }

    private fun submitFragmentIfNeeded(type: FragmentType) {
        if (currentFragment != type) {
            currentFragment = type

            replaceFragment.value = type
            attachFragment.value = type
        }
    }
}