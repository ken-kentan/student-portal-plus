package jp.kentan.student_portal_plus.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.data.component.LectureInformation;
import jp.kentan.student_portal_plus.notification.NotificationController;
import jp.kentan.student_portal_plus.util.LinkTransformationMethod;
import jp.kentan.student_portal_plus.util.StringUtils;

public class LectureInformationActivity extends AppCompatActivity {

    private LectureInformation mInfo;

    private boolean isNotificationMode;
    private boolean isMyClass = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_information);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        /*
        Intent
         */
        Intent intent = getIntent();

        isNotificationMode = intent.getBooleanExtra("notification", false);
        try {
            if(isNotificationMode){
                new NotificationController(this).cancel(intent.getIntExtra("notify_id", -9));

                mInfo = PortalDataProvider.getLectureInfoByHash(this, intent.getStringExtra("hash"));
            }else{
                mInfo = PortalDataProvider.getLectureInfoById(intent.getIntExtra("id", 1));
            }
        } catch (Exception e) {
            failedLoad();
            return;
        }

        mInfo.setRead(true);
        isMyClass = mInfo.isMyClass();

        /*
        UI Initialize
         */
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        final int status = mInfo.getMyClassStatus();
        if (status == LectureInformation.RESISTED_BY_PORTAL) {
            //受講解除できない場合はFAB隠す
            fab.hide();
        } else {
            if(status == LectureInformation.RESISTED_BY_SIMILAR){
                isMyClass = false;
            }

            final Activity activity = this;
            fab.setRotation((isMyClass) ? 135.0f : 0.0f);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if(isMyClass){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle("確認");
                        builder.setMessage(StringUtils.fromHtml(getString(R.string.msg_remove_my_class_info).replaceFirst("\\{subject\\}", mInfo.getSubject())));
                        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(mInfo.setMyClass(isMyClass=false)){
                                    Snackbar.make(view, "受講科目から除外しました", Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                                    fab.animate().rotation(0.0f).setInterpolator(new AnticipateOvershootInterpolator()).withLayer().setDuration(800).start();
                                } else {
                                    final Snackbar snackbar = Snackbar.make(view, getString(R.string.err_msg_failed_to_remove_from_my_class), Snackbar.LENGTH_INDEFINITE);
                                    snackbar.setAction("閉じる", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snackbar.dismiss();
                                        }
                                    }).show();
                                }
                            }
                        });
                        builder.setNegativeButton(getString(R.string.no), null);
                        builder.show();
                    }else{
                        mInfo.setMyClass(isMyClass=true);

                        Snackbar.make(view, "受講科目に追加しました", Snackbar.LENGTH_SHORT).setAction("Action", null).show();

                        fab.animate().rotation(135.0f).setInterpolator(new AnticipateOvershootInterpolator()).withLayer().setDuration(800).start();
                    }
                }
            });
        }


        setTitle(mInfo.getSubject());

        final TextView detail    = (TextView)findViewById(R.id.detail);

        String faculty = mInfo.getFaculty();
        String semester = mInfo.getSemester();

        faculty = (faculty.equals("-")) ? "" : faculty+" ";
        semester = (semester.equals("-")) ? "" : semester+" ";

        ((TextView)findViewById(R.id.subtitle)                   ).setText(mInfo.getInstructor());
        ((TextView)findViewById(R.id.subject)                    ).setText(mInfo.getSubject());
        ((TextView)findViewById(R.id.instructor)                 ).setText(mInfo.getInstructor());
        ((TextView)findViewById(R.id.semester_and_day_and_period)).setText(faculty + " " + semester + "  " + mInfo.getDayAndPeriod());
        ((TextView)findViewById(R.id.type)                       ).setText(mInfo.getType());
        ((TextView)findViewById(R.id.date)                     ).setText(mInfo.getDate());
        detail.setText(StringUtils.fromHtml(mInfo.getDetail()));

        detail.setTransformationMethod(new LinkTransformationMethod(this));
    }

    @Override
    public void onBackPressed() {
        if (isNotificationMode) {
            Intent home = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(home);
        }

        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInfo.getSubject());
                intent.putExtra(Intent.EXTRA_TEXT, createShareText());
                startActivity(intent);
                break;
            default:
                if (isNotificationMode) {
                    Intent home = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(home);
                } else {
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String createShareText() {
        return "授業科目:" + mInfo.getSubject() +
                "\n担当教員:" +
                mInfo.getInstructor() +
                "\n曜日時限:" +
                mInfo.getDayAndPeriod() +
                "\n分類:" +
                mInfo.getType() +
                "\n連絡事項:" +
                StringUtils.removeHtmlTag(mInfo.getDetail()) +
                "\n" +
                mInfo.getDate();
    }

    private void failedLoad() {
        Toast.makeText(this, getString(R.string.err_msg_failed_to_load), Toast.LENGTH_LONG).show();
        finish();
    }
}
