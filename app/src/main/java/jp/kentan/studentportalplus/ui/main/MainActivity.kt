package jp.kentan.studentportalplus.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityMainBinding
import jp.kentan.studentportalplus.notification.SyncScheduler
import jp.kentan.studentportalplus.ui.ViewModelFactory
import jp.kentan.studentportalplus.ui.dashboard.DashboardFragment
import jp.kentan.studentportalplus.ui.lecturecancel.LectureCancelFragment
import jp.kentan.studentportalplus.ui.lectureinfo.LectureInfoFragment
import jp.kentan.studentportalplus.ui.login.LoginActivity
import jp.kentan.studentportalplus.ui.notice.NoticeFragment
import jp.kentan.studentportalplus.ui.setting.SettingsActivity
import jp.kentan.studentportalplus.ui.timetable.TimetableFragment
import jp.kentan.studentportalplus.ui.welcome.WelcomeActivity
import jp.kentan.studentportalplus.util.isAuthenticatedUser
import jp.kentan.studentportalplus.util.isEnabledDetailError
import org.jetbrains.anko.defaultSharedPreferences
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_IS_SYNC = "IS_SYNC"
        private const val EXTRA_FRAGMENT = "FRAGMENT"

        fun createIntent(context: Context, isSync: Boolean = false, fragment: FragmentType? = null) =
                Intent(context, MainActivity::class.java).apply {
                    putExtra(EXTRA_IS_SYNC, isSync)
                    if (fragment != null) {
                        putExtra(EXTRA_FRAGMENT, fragment.name)
                    }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private var finishSnackbar: Snackbar? = null

    private val fragmentMap by lazy(LazyThreadSafetyMode.NONE) {
        mapOf(
                FragmentType.DASHBOARD to DashboardFragment.newInstance(),
                FragmentType.TIMETABLE to TimetableFragment.newInstance(),
                FragmentType.LECTURE_INFO to LectureInfoFragment.newInstance(),
                FragmentType.LECTURE_CANCEL to LectureCancelFragment.newInstance(),
                FragmentType.NOTICE to NoticeFragment.newInstance()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!defaultSharedPreferences.isAuthenticatedUser()) {
            startWelcomeActivity()
            return
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        AndroidInjection.inject(this)

        setSupportActionBar(binding.appBar.toolbar)

        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        binding.setLifecycleOwner(this)
        binding.apply {
            val toggle = ActionBarDrawerToggle(
                    this@MainActivity, drawerLayout, appBar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()

            appBar.refreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorAccent)
            appBar.refreshLayout.setColorSchemeResources(R.color.grey_100)

            viewModel = this@MainActivity.viewModel
        }

        val fragment = when {
            intent.hasExtra(EXTRA_FRAGMENT) -> {
                val name = intent.getStringExtra(EXTRA_FRAGMENT)
                intent = intent.apply { removeExtra(EXTRA_FRAGMENT) }

                fragmentMap[FragmentType.valueOf(name.orEmpty())]
            }
            supportFragmentManager.fragments.isEmpty() -> DashboardFragment.newInstance()
            else -> null
        }

        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
        }

        SyncScheduler(this).scheduleIfNeeded()

        viewModel.subscribe()
        viewModel.onActivityCreated(intent.getBooleanExtra(EXTRA_IS_SYNC, false))
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (supportFragmentManager.backStackEntryCount <= 0) {
                if (viewModel.canFinish()) {
                    finishSnackbar?.dismiss()
                    finish()
                }
                return
            }

            super.onBackPressed()
        }
    }

    private fun MainViewModel.subscribe() {
        val activity = this@MainActivity

        replaceFragment.observe(activity, Observer {
            val newFragment = fragmentMap[it] ?: return@Observer

            supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.container, newFragment)
                    .addToBackStack(null)
                    .commit()
        })

        attachFragment.observe(activity, Observer { it.attach() })

        user.observe(activity, Observer { user ->
            binding.navView.getHeaderView(0).apply {
                findViewById<TextView>(R.id.name).text = user.name
                findViewById<TextView>(R.id.username).text = user.username
            }
        })

        closeDrawer.observe(activity, Observer {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        })

        openMap.observe(activity, Observer { MapHelper.open(activity, it) })

        startSettingsActivity.observe(activity, Observer {
            startActivity(Intent(activity, SettingsActivity::class.java))
        })

        indefiniteSnackbar.observe(activity, Observer { (message, isAuthError) ->
            val snackbar = Snackbar.make(binding.root, R.string.error_unknown, Snackbar.LENGTH_INDEFINITE)

            if (isAuthError) {
                if (message == null) {
                    snackbar.setText(R.string.msg_request_shibboleth_data)
                } else {
                    snackbar.setText("$message\n${getString(R.string.msg_request_shibboleth_data)}")
                }

                snackbar.setAction(R.string.action_login) {
                    startActivity(LoginActivity.createIntent(activity, true))
                }
            } else {
                if (message != null && defaultSharedPreferences.isEnabledDetailError()) {
                    snackbar.setText(message)
                } else {
                    snackbar.setText(R.string.error_failed_to_sync)
                }

                snackbar.setAction(R.string.action_close) { snackbar.dismiss() }
            }

            snackbar.show()
        })

        finishSnackbar.observe(activity, Observer { callback ->
            val snackbar = Snackbar.make(binding.root, getString(R.string.msg_back_to_exit), Snackbar.LENGTH_LONG)
                    .addCallback(callback)

            snackbar.show()

            activity.finishSnackbar = snackbar
        })
    }

    private fun startWelcomeActivity() {
        startActivity(WelcomeActivity.createIntent(this))
    }

    private fun FragmentType.attach() {
        title = getString(titleResId)
        binding.navView.menu.findItem(menuItemId).isChecked = true
    }
}
