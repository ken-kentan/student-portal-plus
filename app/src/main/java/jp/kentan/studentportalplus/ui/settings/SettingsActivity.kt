package jp.kentan.studentportalplus.ui.settings

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import dagger.android.support.DaggerAppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.ActivitySettingsBinding
import jp.kentan.studentportalplus.util.findNavController

class SettingsActivity : DaggerAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)
            .apply {
                val appBarConfiguration = AppBarConfiguration.Builder()
                    .setFallbackOnNavigateUpListener {
                        finish()
                        return@setFallbackOnNavigateUpListener true
                    }.build()

                NavigationUI.setupWithNavController(
                    toolbar,
                    supportFragmentManager.findNavController(),
                    appBarConfiguration
                )
            }
    }
}
