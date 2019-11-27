package jp.kentan.studentportalplus.ui.settings

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivitySettingsBinding

class SettingsActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)
            .apply {
                setSupportActionBar(toolbar)
                setupWithNavController()
            }
    }

    private fun ActivitySettingsBinding.setupWithNavController() {
        val navController = findNavController(this@SettingsActivity, R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration.Builder()
            .setFallbackOnNavigateUpListener {
                finish()
                return@setFallbackOnNavigateUpListener true
            }.build()

        setupActionBarWithNavController(
            navController,
            appBarConfiguration
        )
        toolbar.setNavigationOnClickListener {
            navController.navigateUp(appBarConfiguration)
        }
    }
}
