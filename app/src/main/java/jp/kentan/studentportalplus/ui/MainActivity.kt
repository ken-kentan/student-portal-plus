package jp.kentan.studentportalplus.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import jp.kentan.studentportalplus.ui.welcome.WelcomeActivity
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
                        R.id.navigation_dashboard,
                        R.id.navigation_timetable,
                        R.id.navigation_lecture_informations,
                        R.id.navigation_lecture_cancellations,
                        R.id.navigation_notices
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(WelcomeActivity.createIntent(this))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
