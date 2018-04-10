package jp.kentan.studentportalplus.ui

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.ui.fragment.*
import jp.kentan.studentportalplus.ui.span.CustomTitle
import jp.kentan.studentportalplus.ui.widget.MapView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.intentFor
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    enum class FragmentType{DASHBOARD, TIMETABLE, LECTURE_INFO, LECTURE_CANCEL, NOTICE}

    private var fragmentType = FragmentType.DASHBOARD

    private val fragmentMap by lazy {
        mapOf(
                FragmentType.DASHBOARD to DashboardFragment.newInstance(),
                FragmentType.TIMETABLE to TimetableFragment.newInstance(),
                FragmentType.LECTURE_INFO to LectureInformationFragment.newInstance(),
                FragmentType.LECTURE_CANCEL to LectureCancellationFragment.newInstance(),
                FragmentType.NOTICE to NoticeFragment.newInstance())
    }

    @Inject
    lateinit var portalRepository: PortalRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (isFirstLaunch()) {
            launchWelcomeActivity()
            return
        }

        AndroidInjection.inject(this)

        fab.setOnClickListener { view ->
            bg{
                portalRepository.syncWithWeb()
            }
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        setupSwipeRefresh()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DashboardFragment.newInstance())
                    .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        bg{
            portalRepository.loadFromDb()
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)

        async(UI) {
            delay(300)

            when (item.itemId) {
                R.id.nav_dashboard      -> { switchFragment(FragmentType.DASHBOARD) }
                R.id.nav_timetable      -> { switchFragment(FragmentType.TIMETABLE) }
                R.id.nav_lecture_info   -> { switchFragment(FragmentType.LECTURE_INFO) }
                R.id.nav_lecture_cancel -> { switchFragment(FragmentType.LECTURE_CANCEL) }
                R.id.nav_notice         -> { switchFragment(FragmentType.NOTICE) }
                R.id.nav_campus_map     -> { MapView.open(this@MainActivity, MapView.Type.CAMPUS) }
                R.id.nav_room_map       -> { MapView.open(this@MainActivity, MapView.Type.ROOM) }
                R.id.nav_setting        -> {  }
            }
        }

        return true
    }

    override fun onAttachFragment(fragment: Fragment?) {
        if (nav_view != null) {
            when (fragment) {
                is DashboardFragment -> {
                    fragmentType = FragmentType.DASHBOARD

                    title = CustomTitle(this, getString(R.string.title_dashboard_fragment))
                    nav_view.menu.findItem(R.id.nav_dashboard).isChecked = true
                }
                is TimetableFragment -> {
                    fragmentType = FragmentType.TIMETABLE

                    title = CustomTitle(this, getString(R.string.title_timetable_fragment))
                    nav_view.menu.findItem(R.id.nav_timetable).isChecked = true
                }
                is LectureInformationFragment -> {
                    fragmentType = FragmentType.LECTURE_INFO

                    title = CustomTitle(this, getString(R.string.title_lecture_info_fragment))
                    nav_view.menu.findItem(R.id.nav_lecture_info).isChecked = true
                }
                is LectureCancellationFragment -> {
                    fragmentType = FragmentType.LECTURE_CANCEL

                    title = CustomTitle(this, getString(R.string.title_lecture_cancel_fragment))
                    nav_view.menu.findItem(R.id.nav_lecture_cancel).isChecked = true
                }
                is NoticeFragment -> {
                    fragmentType = FragmentType.NOTICE

                    title = CustomTitle(this, getString(R.string.title_notice_fragment))
                    nav_view.menu.findItem(R.id.nav_notice).isChecked = true
                }
            }
        }

        super.onAttachFragment(fragment)
    }

    private fun setupSwipeRefresh() {
        swipe_refresh_layout.setProgressBackgroundColorSchemeResource(R.color.colorAccent)
        swipe_refresh_layout.setColorSchemeResources(R.color.grey_100)
        swipe_refresh_layout.setOnRefreshListener {
            async(UI) {
                val result = bg {portalRepository.syncWithWeb()}

                val (success, message) = result.await()

                swipe_refresh_layout.isRefreshing = false

                if (success) {
                    return@async
                }

                val snackbar = Snackbar.make(fab, message ?: "null", Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction(getString(R.string.action_close), { snackbar.dismiss() })
                snackbar.show()
            }
        }
    }

    fun switchFragment(type: FragmentType) {
        if (fragmentType == type) {
            return
        }

        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, fragmentMap[type])
                .addToBackStack(null)
                .commit()

        fragmentType = type
    }

    private fun isFirstLaunch(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getBoolean("is_first", true)
    }

    private fun launchWelcomeActivity() {
        val intent = intentFor<WelcomeActivity>()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }
}
