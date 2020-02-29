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
import jp.kentan.studentportalplus.util.findNavController
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private const val EXTRA_START_DESTINATION = "START_DESTINATION"

        fun createIntent(context: Context, startDestination: Destination? = null) =
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

                if (startDestination != null) {
                    putExtra(EXTRA_START_DESTINATION, startDestination)
                }
            }
    }

    enum class Destination(
        @IdRes val resId: Int
    ) {
        LECTURE_INFORMATION(R.id.lecture_informations_fragment),
        LECTURE_CANCELLATION(R.id.lecture_cancellations_fragment),
        NOTICE(R.id.notices_fragment)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val mainViewModel by viewModels<MainViewModel> { viewModelFactory }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                lifecycleOwner = this@MainActivity
                viewModel = mainViewModel

                setSupportActionBar(appBar.toolbar)

                val navController = supportFragmentManager.findNavController()

                setupWithNavController(
                    navView,
                    drawerLayout,
                    navController,
                    intent.getSerializableExtra(EXTRA_START_DESTINATION) as Destination?
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
        mainViewModel.indefiniteSnackbar.observeEvent(this) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_INDEFINITE).apply {
                setAction(R.string.all_close) { dismiss() }
            }.show()
        }
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
        startDestination: Destination?
    ) {
        val fragmentIdSet = setOf(
            R.id.dashboard_fragment,
            R.id.timetable_fragment,
            R.id.lecture_informations_fragment,
            R.id.lecture_cancellations_fragment,
            R.id.notices_fragment
        )

        navigationView.setNavigationItemSelectedListener { item ->
            val builder = NavOptions.Builder()
                .setLaunchSingleTop(true)

            if (fragmentIdSet.contains(item.itemId)) {
                builder.setPopUpTo(R.id.dashboard_fragment, false)
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

        if (startDestination != null) {
            navController.navigate(startDestination.resId)
        }
    }
}
