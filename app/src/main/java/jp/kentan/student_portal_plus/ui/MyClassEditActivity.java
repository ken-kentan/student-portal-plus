package jp.kentan.student_portal_plus.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.util.StringUtils;

public class MyClassEditActivity extends AppCompatActivity{

    private final static String TAG = "MyClassEditActivity";
    public final static int REGISTER_MODE = 0, UPDATE_MODE = 1, UPDATE_LIMIT_MODE = 2;

    private View mViewColor;
    private TextInputEditText mEditSubject, mEditInstructor, mEditPlace, mEditType, mEditCredits, mEditTimetableNumber;
    private Spinner mSpinnerWeek, mSpinnerPeriod;

    private MyClass mInfo = null;
    private int mEditMode, mColorRgb;

    //戻る時に編集内容が変化しているかの確認用
    private String SUBJECT = "", INSTRUCTOR = "", PLACE = "", TYPE = "", CREDITS = "", TIMETABLE_NUM = "";
    private int DAY_OF_WEEK = 0, PERIOD = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_class_edit);

        /*
        Intent
         */
        final int infoId;
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            infoId = intent.getIntExtra("id", -1);
            mEditMode = intent.getIntExtra("edit_mode", REGISTER_MODE);

            if(infoId < 0) mColorRgb = MyClass.DEFAULT_COLOR;
        } else {
            infoId = savedInstanceState.getInt("INFO_ID");
            mEditMode = savedInstanceState.getInt("EDIT_MODE");
            mColorRgb = savedInstanceState.getInt("COLOR");
        }

        if(infoId > 0){
            try {
                mInfo = PortalDataProvider.getMyClassById(infoId);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                failedLoad();
                return;
            }
        }


        /*
        UI Initialize
         */
        mViewColor = findViewById(R.id.color);

        mEditSubject         = (TextInputEditText)findViewById(R.id.subject);
        mEditInstructor      = (TextInputEditText)findViewById(R.id.instructor);
        mEditPlace           = (TextInputEditText)findViewById(R.id.place);
        mEditType            = (TextInputEditText)findViewById(R.id.type);
        mEditCredits         = (TextInputEditText)findViewById(R.id.credits);
        mEditTimetableNumber = (TextInputEditText)findViewById(R.id.timetable_number);

        mSpinnerWeek   = (Spinner)findViewById(R.id.week);
        mSpinnerPeriod = (Spinner)findViewById(R.id.date);

        mSpinnerWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (mEditMode == UPDATE_LIMIT_MODE) {
                    mSpinnerPeriod.setEnabled(false);
                } else {
                    mSpinnerPeriod.setEnabled(pos < 7);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mViewColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                colorPickerDialog.initialize(
                        R.string.title_color_dialog, MyClass.COLORS, mColorRgb, 4, MyClass.COLORS.length);
                colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int color) {
                        mColorRgb = color;
                        mViewColor.setBackgroundColor(color);
                    }
                });
                colorPickerDialog.show(getFragmentManager(), "ColorPickerDialog");
            }

        });


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setTitle(StringUtils.fromHtml("<font color='#1e2128'>" + ((mEditMode == REGISTER_MODE) ? "受講科目の追加" : "受講科目の編集") + "</font>"));
            actionBar.setBackgroundDrawable(new ColorDrawable(0xFFEEEEEE));
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        if(savedInstanceState == null || mEditMode == UPDATE_LIMIT_MODE){
            if(savedInstanceState == null && mInfo != null) mColorRgb = mInfo.getColor();

            setInfoData();
        }

        mViewColor.setBackgroundColor(mColorRgb);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("EDIT_MODE", mEditMode);
        outState.putInt("INFO_ID", (mInfo != null) ? mInfo.getId() : -1);

        outState.putInt("COLOR", mColorRgb);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_class_edit, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                break;
            default:
                if (wasEditedByUser()) {
                    showDiscardDialog();
                } else {
                    exit();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        View focusView = null;

        int colorRgb   = ((ColorDrawable)mViewColor.getBackground()).getColor();

        String subject    = mEditSubject   .getText().toString();
        String instructor = mEditInstructor.getText().toString();
        String place      = mEditPlace     .getText().toString();

        int dayOfWeek = (int)mSpinnerWeek  .getSelectedItemId();
        int period    = (int)mSpinnerPeriod.getSelectedItemId() + 1;

        String type       = mEditType           .getText().toString();
        String strCredits = mEditCredits        .getText().toString();
        String timetable  = mEditTimetableNumber.getText().toString();

        int credits = -1;

        //スペースとかを除去
        if (StringUtils.isBlank(instructor)) instructor = null;
        if (StringUtils.isBlank(place))      place      = null;
        if (StringUtils.isBlank(type))       type       = null;


        if (!isSubjectValid(subject)) {
            focusView = mEditSubject;
        }

        if (!isCreditsValid(strCredits)) {
            focusView = (focusView == null) ? mEditCredits : focusView;
        } else if (!StringUtils.isBlank(strCredits)) {
            credits = Integer.parseInt(strCredits);
        }

        if (!isTimetableNumValid(timetable)) {
            focusView = (focusView == null) ? mEditTimetableNumber : focusView;
        }

        //集中科目の場合、時限なし
        if (dayOfWeek >= 7) {
            period = -1;
        }

        if (focusView == null) {
            switch (mEditMode) {
                case REGISTER_MODE:
                    PortalDataProvider.registerToMyClass(subject, instructor, place, type, dayOfWeek, period, credits, normalizeTimetableNum(timetable), colorRgb);
                    break;
                case UPDATE_MODE:
                case UPDATE_LIMIT_MODE:
                    mInfo.update(dayOfWeek, period, subject, instructor, place, type, credits, normalizeTimetableNum(timetable), colorRgb);
                    break;
            }
            exit();
        } else {
            focusView.requestFocus();
        }
    }

    @Override
    public void finish() {
        if (wasEditedByUser()) {
            showDiscardDialog();
        } else {
            exit();
        }
    }

    private void setInfoData() {
        if(mEditMode == REGISTER_MODE) return;

        mEditSubject   .setText(SUBJECT    = mInfo.getSubject());
        mEditInstructor.setText(INSTRUCTOR = mInfo.getInstructor());
        mEditPlace     .setText(PLACE      = mInfo.getPlace());
        mEditType      .setText(TYPE       = mInfo.getType());


        final int dayOfWeek = DAY_OF_WEEK = mInfo.getDayOfWeek();
        mSpinnerWeek.setSelection(dayOfWeek);

        if (dayOfWeek <= 7) {
            mSpinnerPeriod.setSelection(PERIOD = mInfo.getPeriod() - 1);
        } else {
            mSpinnerPeriod.setEnabled(false);
        }


        final int credits = mInfo.getCredits();
        CREDITS = Integer.toString(credits);
        mEditCredits.setText((credits > 0) ? Integer.toString(credits) : "");


        final int timetableNum = mInfo.getTimeTableNumber();
        TIMETABLE_NUM = Integer.toString(timetableNum);
        mEditTimetableNumber.setText((timetableNum > 0) ? Integer.toString(timetableNum) : "");


        if (mEditMode == UPDATE_LIMIT_MODE) {
            mEditSubject        .setEnabled(false);
            mEditInstructor     .setEnabled(false);
            mEditType           .setEnabled(false);
            mEditCredits        .setEnabled(false);
            mEditTimetableNumber.setEnabled(false);
            mSpinnerWeek        .setEnabled(false);
            mSpinnerPeriod      .setEnabled(false);
        }
    }

    private void showDiscardDialog() {
        final MyClassEditActivity activity = this;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("確認");
        builder.setMessage("編集中の内容を破棄しますか？");
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.exit();
            }
        });
        builder.setNegativeButton(getString(R.string.no), null);
        builder.show();
    }

    private boolean wasEditedByUser() {
        final String subject    = mEditSubject        .getText().toString();
        final String instructor = mEditInstructor     .getText().toString();
        final String place      = mEditPlace          .getText().toString();
        final String type       = mEditType           .getText().toString();
        final String credits    = mEditCredits        .getText().toString();
        final String timetable  = mEditTimetableNumber.getText().toString();

        final int dayOfWeek = (int)mSpinnerWeek.getSelectedItemId();
        final int period    = (int)mSpinnerPeriod.getSelectedItemId();

        //Normalize
        if (INSTRUCTOR == null) INSTRUCTOR = "";
        if (PLACE      == null) PLACE      = "";
        if (TYPE       == null) TYPE       = "";
        if (CREDITS.equals(      "-1")) CREDITS       = "";
        if (TIMETABLE_NUM.equals("-1")) TIMETABLE_NUM = "";

        final int COLOR = (mInfo != null) ? mInfo.getColor() : MyClass.DEFAULT_COLOR;

        boolean isSamePeriod = true;
        if(dayOfWeek < 7){
            isSamePeriod = (PERIOD == period);
        }

        return !(SUBJECT.equals(subject) && INSTRUCTOR.equals(instructor) && PLACE.equals(place) &&
                TYPE.equals(type) && CREDITS.equals(credits) && TIMETABLE_NUM.equals(timetable) &&
                DAY_OF_WEEK == dayOfWeek && isSamePeriod && COLOR == mColorRgb);
    }

    private boolean isSubjectValid(String strSubject) {
        if (StringUtils.isBlank(strSubject)) {
            mEditSubject.setError(getString(R.string.error_field_required));
            return false;
        }

        return true;
    }

    private boolean isCreditsValid(String strCredits) {
        if (StringUtils.isBlank(strCredits)) return true;

        try {
            int credit = Integer.parseInt(strCredits);

            if (credit <= 0 || credit > 10) {
                mEditCredits.setError("不正な単位数です");
                return false;
            }
        } catch (Exception e) {
            mEditCredits.setError("不正な入力です");
            return false;
        }

        return true;
    }

    private boolean isTimetableNumValid(String strNum) {
        if (StringUtils.isBlank(strNum)) return true;

        try {
            int num = Integer.parseInt(strNum);
            if (num < 10000000 || num > 1000000000) {
                mEditTimetableNumber.setError("不正な時間割番号です");
                return false;
            }
        } catch (Exception e) {
            mEditTimetableNumber.setError("不正な入力です");
            return false;
        }

        return true;
    }

    private int normalizeTimetableNum(String strNum) {
        if (StringUtils.isBlank(strNum)) {
            return -1;
        }

        int num = Integer.parseInt(strNum);

        return (num >= 10000000) ? num : -1;
    }

    private void failedLoad() {
        Toast.makeText(this, getString(R.string.err_msg_failed_to_load), Toast.LENGTH_LONG).show();
        finish();
    }

    public void exit() {
        super.finish();
    }
}
