package jp.kentan.studentportalplus.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityMainBinding
import jp.kentan.studentportalplus.databinding.NavHeaderMainBinding
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    companion object {
        fun createIntent(context: Context) =
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
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

                val navController = findNavController(R.id.nav_host_fragment)
                setupWithNavController(navView, drawerLayout, navController)

                val toggle = ActionBarDrawerToggle(
                    this@MainActivity, drawerLayout, appBar.toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
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
                setAction(R.string.action_close) { dismiss() }
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
        navController: NavController
    ) {
        val fragmentSet = setOf(
            R.id.dashboard_fragment,
            R.id.timetable_fragment,
            R.id.lecture_informations_fragment,
            R.id.lecture_cancellations_fragment,
            R.id.notices_fragment
        )

        navigationView.setNavigationItemSelectedListener { item ->
            val builder = NavOptions.Builder()
                .setLaunchSingleTop(true)

            if (fragmentSet.contains(item.itemId)) {
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
    }
}
