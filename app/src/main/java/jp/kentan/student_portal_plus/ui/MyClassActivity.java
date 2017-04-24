package jp.kentan.student_portal_plus.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.util.LinkTransformationMethod;
import jp.kentan.student_portal_plus.util.StringUtils;


public class MyClassActivity extends AppCompatActivity {

    private int mInfoId;
    private boolean hasDeletedByUser = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_class);
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
        mInfoId = intent.getIntExtra("id", 1);
        final MyClass info;
        try {
            info = PortalDataProvider.getMyClassById(mInfoId);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.err_msg_failed_to_load), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

         /*
        UI Initialize
         */
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        final MyClassActivity activity = this;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, MyClassEditActivity.class);
                intent.putExtra("edit_mode", (info.hasRegisteredByUser()) ? MyClassEditActivity.UPDATE_MODE : MyClassEditActivity.UPDATE_LIMIT_MODE);
                intent.putExtra("id", mInfoId);
                startActivity(intent);
                hasDeletedByUser = true;
            }
        });


        updateInformationTextViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateInformationTextViews();
    }

    private void updateInformationTextViews() {
        MyClass info;
        try {
            info = PortalDataProvider.getMyClassById(mInfoId);
        } catch (Exception e) {
            if (!hasDeletedByUser) {
                Toast.makeText(this, getString(R.string.err_msg_failed_to_load), Toast.LENGTH_LONG).show();
            }

            finish();
            return;
        }


        final String subject    = info.getSubject();
        final String instructor = info.getInstructor();
        final String place      = info.getPlace();
        final String type       = info.getType();
        final int    credits    = info.getCredits();

        final StringBuilder typeAndCredits = new StringBuilder((StringUtils.isBlank(type) ? "" : type));


        //Layout initialize
        final CollapsingToolbarLayout layout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        layout.setBackgroundColor(info.getColor());
        layout.setTitle(subject);


        ((TextView)findViewById(R.id.subtitle)  ).setText(instructor);
        ((TextView)findViewById(R.id.subject)   ).setText(subject);
        ((TextView)findViewById(R.id.instructor)).setText((StringUtils.isBlank(instructor)) ? "未入力" : instructor);
        ((TextView)findViewById(R.id.place)     ).setText( (StringUtils.isBlank(place)    ) ? "未入力" : place);


        //分類View に複数の情報をまとめて表示
        final TextView typeView = (TextView) findViewById(R.id.type);

        if (credits > 0) {
            if(!StringUtils.isBlank(type)) typeAndCredits.append(" ");
            typeAndCredits.append(credits);
            typeAndCredits.append("単位");
        }

        if (StringUtils.isBlank(type) && credits <= 0) {
            typeView.setText("未入力");
        }else{
            typeView.setText(typeAndCredits.toString());
        }


        //曜日時限
        final int dayOfWeek = info.getDayOfWeek();
        String strDayOfWeek = MyClass.DAY_OF_WEEK[dayOfWeek];

        if (dayOfWeek < 7) {
            strDayOfWeek += "曜 " + info.getPeriod() + "限";
        }
        ((TextView)findViewById(R.id.day_and_period)).setText(strDayOfWeek);


        //Web Syllabus
        String webSyllabus = "時間割番号が未入力";
        if (info.getTimeTableNumber() > 0) {
            webSyllabus = info.getWebSyllabusUrl();
        }

        final TextView syllabusView = (TextView)findViewById(R.id.web_syllabus);
        syllabusView.setText(webSyllabus);
        syllabusView.setTransformationMethod(new LinkTransformationMethod(this));
    }
}
