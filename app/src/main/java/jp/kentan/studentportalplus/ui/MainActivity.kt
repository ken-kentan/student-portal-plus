package jp.kentan.studentportalplus.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivityMainBinding
import jp.kentan.studentportalplus.databinding.NavHeaderMainBinding
import jp.kentan.studentportalplus.util.findNavController
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

                val navController = supportFragmentManager.findNavController()
                NavigationUI.setupWithNavController(navView, navController)

                val appBarConfiguration = AppBarConfiguration.Builder(
                    setOf(
                        R.id.dashboard_fragment,
                        R.id.timetable_fragment,
                        R.id.lecture_informations_fragment,
                        R.id.lecture_cancellations_fragment,
                        R.id.notices_fragment
                    )
                ).setDrawerLayout(drawerLayout).build()

                setupActionBarWithNavController(navController, appBarConfiguration)

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
    }

    override fun onBackPressed() {
        val drawerLayout = binding.drawerLayout
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
