package jp.kentan.studentportalplus.ui.shortcut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.ui.main.FragmentType
import jp.kentan.studentportalplus.ui.main.MainActivity

class TimetableShortcutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutIntent = MainActivity.createIntent(this, fragment = FragmentType.TIMETABLE)
        shortcutIntent.action = intent.action

        val shortcut = ShortcutInfoCompat.Builder(this, "shortcut_timetable")
                .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_timetable))
                .setShortLabel(getString(R.string.shortcut_timetable))
                .setIntent(shortcutIntent)
                .build()

        setResult(RESULT_OK, ShortcutManagerCompat.createShortcutResultIntent(this, shortcut))

        finish()
    }
}