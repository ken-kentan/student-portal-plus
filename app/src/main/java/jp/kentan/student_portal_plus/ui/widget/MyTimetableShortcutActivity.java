package jp.kentan.student_portal_plus.ui.widget;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.ui.HomeActivity;


public class MyTimetableShortcutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent shortcutIntent = new Intent(this, HomeActivity.class);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        shortcutIntent.putExtra("view_mode", 2);

        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_my_timetable);

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.title_my_timetable));

        setResult(RESULT_OK, intent);

        finish();
    }
}
