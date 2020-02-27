package jp.kentan.studentportalplus.ui.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ui.setupWithNavController
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.findNavController

class WelcomeActivity : AppCompatActivity(R.layout.activity_welcome) {

    companion object {
        fun createIntent(context: Context) = Intent(context, WelcomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = supportFragmentManager.findNavController()
        findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController)
    }
}
