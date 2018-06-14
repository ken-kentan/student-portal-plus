package jp.kentan.studentportalplus.ui.widget

import android.os.Bundle
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import android.support.v7.app.AppCompatActivity
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.ui.MainActivity
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask


class TimetableShortcutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutIntent = intentFor<MainActivity>("fragment_type" to MainActivity.FragmentType.TIMETABLE.name).newTask().clearTop()
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