package jp.kentan.studentportalplus.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityMainBinding
import jp.kentan.studentportalplus.databinding.NavHeaderMainBinding
import jp.kentan.studentportalplus.ui.welcome.WelcomeActivity
import jp.kentan.studentportalplus.util.findNavController
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private const val EXTRA_NAVIGATE = "NAVIGATE"
        private const val EXTRA_SHOULD_REFRESH = "SHOULD_REFRESH"
        private const val EXTRA_IS_TIMETABLE_START_DESTINATION = "IS_TIMETABLE_START_DESTINATION"

        private const val RESOURCE_ID_NULL = 0

        fun createIntent(context: Context, @IdRes navigateResId: Int? = null) =
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

                if (navigateResId != null) {
                    putExtra(EXTRA_NAVIGATE, navigateResId)
                }
            }

        fun createIntent(
            context: Context,
            shouldRefresh: Boolean,
            isTimetableStartDestination: Boolean
        ) = createIntent(context).apply {
            putExtra(EXTRA_SHOULD_REFRESH, shouldRefresh)
            putExtra(EXTRA_IS_TIMETABLE_START_DESTINATION, isTimetableStartDestination)
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mainViewModel by viewModels<MainViewModel> { viewModelFactory }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mainViewModel.shouldLaunchWelcomeActivity) {
            startActivity(WelcomeActivity.createIntent(this))
            return
        }

        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                lifecycleOwner = this@MainActivity
                viewModel = mainViewModel

                setSupportActionBar(appBar.toolbar)

                setupWithNavController(
                    navView,
                    drawerLayout,
                    supportFragmentManager.findNavController(),
                    intent.getIntExtra(EXTRA_NAVIGATE, RESOURCE_ID_NULL),
                    intent.getBooleanExtra(EXTRA_IS_TIMETABLE_START_DESTINATION, false)
                )

                val toggle = ActionBarDrawerToggle(
                    this@MainActivity, drawerLayout, appBar.toolbar,
                    R.string.main_open_navigation_drawer,
                    R.string.main_close_navigation_drawer
                )
                drawerLayout.addDrawerListener(toggle)
                toggle.syncState()
            }

        val navBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0))
        mainViewModel.user.observe(this) {
            navBinding.user = it
        }
        mainViewModel.closeDrawer.observeEvent(this) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        mainViewModel.errorSnackbar.observeEvent(this) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_INDEFINITE).apply {
                setAction(R.string.all_close) { dismiss() }
            }.show()
        }
        mainViewModel.loginSnackbar.observeEvent(this) {
            Snackbar.make(
                binding.root,
                R.string.main_authentication_failed_error,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.main_login) {
                supportFragmentManager.findNavController().navigate(R.id.settings_activity)
            }.show()
        }

        mainViewModel.onCreate(
            shouldRefresh = intent.getBooleanExtra(EXTRA_SHOULD_REFRESH, false)
        )
    }

    override fun onBackPressed() {
        val drawerLayout = binding.drawerLayout
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setupWithNavController(
        navigationView: NavigationView,
        drawerLayout: DrawerLayout,
        navController: NavController,
        @IdRes navigateResId: Int,
        isTimetableStartDestination: Boolean
    ) {
        val navGraph = navController.navInflater.inflate(R.navigation.main_graph).apply {
            if (isTimetableStartDestination) {
                startDestination = R.id.timetable_fragment
            }
        }
        navController.graph = navGraph

        val fragmentIdSet = setOf(
            R.id.dashboard_fragment,
            R.id.timetable_fragment,
            R.id.lecture_informations_fragment,
            R.id.lecture_cancellations_fragment,
            R.id.notices_fragment
        )
        val popUpToDestinationId = if (isTimetableStartDestination) {
            R.id.timetable_fragment
        } else {
            R.id.dashboard_fragment
        }

        navigationView.setNavigationItemSelectedListener { item ->
            if (navController.currentDestination?.id == item.itemId) {
                drawerLayout.closeDrawer(navigationView)
                return@setNavigationItemSelectedListener true
            }

            val builder = NavOptions.Builder()

            if (fragmentIdSet.contains(item.itemId)) {
                builder.setPopUpTo(popUpToDestinationId, false)
                    .setEnterAnim(androidx.navigation.ui.R.anim.nav_default_enter_anim)
                    .setExitAnim(androidx.navigation.ui.R.anim.nav_default_exit_anim)
                    .setPopEnterAnim(androidx.navigation.ui.R.anim.nav_default_pop_enter_anim)
                    .setPopExitAnim(androidx.navigation.ui.R.anim.nav_default_pop_exit_anim)
            }

            return@setNavigationItemSelectedListener try {
                navController.navigate(item.itemId, null, builder.build())
                drawerLayout.closeDrawer(navigationView)

                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            title = destination.label

            navigationView.menu.forEach { item ->
                item.isChecked = destination.id == item.itemId
            }
        }

        if (navigateResId != RESOURCE_ID_NULL) {
            navController.navigate(navigateResId)
        }
    }
}
