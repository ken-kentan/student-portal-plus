package jp.kentan.studentportalplus.ui.timetable.shortcut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.ui.MainActivity

class TimetableShortcutActivity : AppCompatActivity() {

    companion object {
        private const val SHORTCUT_ID = "shortcut_timetable"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutIntent = MainActivity.createIntent(
            context = this,
            shouldRefresh = false,
            isTimetableStartDestination = true
        ).apply {
            action = intent.action
        }

        val shortcut = ShortcutInfoCompat.Builder(this, SHORTCUT_ID)
            .setIcon(IconCompat.createWithResource(this, R.mipmap.timetable_shortcut_activity))
            .setShortLabel(getString(R.string.timetable_shortcut_label))
            .setIntent(shortcutIntent)
            .build()

        setResult(RESULT_OK, ShortcutManagerCompat.createShortcutResultIntent(this, shortcut))

        finish()
    }
}
