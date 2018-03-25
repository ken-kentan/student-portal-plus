package jp.kentan.studentportalplus.ui

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.PortalRepository
import jp.kentan.studentportalplus.ui.fragment.DashboardFragment
import jp.kentan.studentportalplus.ui.fragment.NoticeFragment
import jp.kentan.studentportalplus.ui.span.CustomTitle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.intentFor
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        enum class FragmentType{DASHBOARD, NOTICE}
    }

    private var fragmentType = FragmentType.DASHBOARD

    private val fragmentMap by lazy {
        mapOf(
                FragmentType.DASHBOARD to DashboardFragment.instance,
                FragmentType.NOTICE    to NoticeFragment.instance)
    }

    @Inject
    lateinit var portalRepository: PortalRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Launch Login activity if need
        if (isFirstLaunch()) {
            launchWelcomeActivity()
            return
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        super.onCreate(savedInstanceState)

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
                    .replace(R.id.fragment_container, DashboardFragment.instance)
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

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        when (item.itemId) {
//            R.id.action_settings -> return true
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)

        async(UI) {
            delay(300)

            when (item.itemId) {
                R.id.nav_dashboard -> { switchFragment(FragmentType.DASHBOARD) }
                R.id.nav_timetable -> {  }
                R.id.nav_lecture_info -> {  }
                R.id.nav_lecture_cancel -> {  }
                R.id.nav_notice -> { switchFragment(FragmentType.NOTICE) }
                R.id.nav_campus_map -> {

                }
                R.id.nav_room_map -> {

                }
                R.id.nav_setting -> {

                }
            }
        }

        return true
    }

    override fun onAttachFragment(fragment: Fragment?) {
        when (fragment) {
            is DashboardFragment -> {
                fragmentType = FragmentType.DASHBOARD

                title = CustomTitle(this, getString(R.string.title_dashboard_fragment))
                nav_view.menu.findItem(R.id.nav_dashboard).isChecked = true
            }
            is NoticeFragment -> {
                fragmentType = FragmentType.NOTICE

                title = CustomTitle(this, getString(R.string.title_notice_fragment))
                nav_view.menu.findItem(R.id.nav_notice).isChecked = true
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

    private fun switchFragment(type: FragmentType) {
        if (fragmentType == type) {
            return
        }

        supportFragmentManager.beginTransaction()
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
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
