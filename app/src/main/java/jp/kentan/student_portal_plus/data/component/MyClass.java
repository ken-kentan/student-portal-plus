package jp.kentan.student_portal_plus.data.component;


import android.graphics.Color;

import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.util.StringUtils;


public class MyClass {

    public final static String URL = "https://portal.student.kit.ac.jp/ead/?c=attend_course";
    public final static String KEY = "my_class";
    public final static String KEY_DATE = "my_class_last_date";

    public final static String[] DAY_OF_WEEK = {"月", "火", "水", "木", "金", "土", "日", "集中", "-"};
    private final static String WEB_SYLLABUS_TEMPLATE = "http://www.syllabus.kit.ac.jp/?c=detail&schedule_code=";

    public final static int RESISTED_BY_PORTAL = 0;

    public final static int DEFAULT_COLOR = Color.parseColor("#4FC3F7"); //Light Blue 300
    public final static int[] COLORS = {
            Color.parseColor("#4FC3F7"), Color.parseColor("#03A9F4"), Color.parseColor("#0288D1"), Color.parseColor("#01579B"), //Light Blue 300 500 700 900
            Color.parseColor("#1B5E20"), Color.parseColor("#388E3C"), Color.parseColor("#4CAF50"), Color.parseColor("#81C784"), //Green      900 700 500 300
            Color.parseColor("#E57373"), Color.parseColor("#F44336"), Color.parseColor("#D32F2F"), Color.parseColor("#B71C1C"), //Red        300 500 700 900
            Color.parseColor("#E65100"), Color.parseColor("#F57C00"), Color.parseColor("#FF9800"), Color.parseColor("#FFB74D"), //Orange     900 700 500 300
            Color.parseColor("#BA68C8"), Color.parseColor("#9C27B0"), Color.parseColor("#7B1FA2"), Color.parseColor("#4A148C"), //Purple     300 500 700 900
            Color.parseColor("#3E2723"), Color.parseColor("#5D4037"), Color.parseColor("#795548"), Color.parseColor("#A1887F"), //Brown      900 700 500 300
    };

    private final int mId;

    private int mDayOfWeek, mPeriod, mCredits, mTimetableNumber, mColorRgb;
    private String mSubject, mInstructor, mPlace, mType;
    private final boolean hasRegisteredByUser;


    public MyClass(final int id, final int dayOfWeek, final int period, final String subject, final String instructor, final String place, final String type, final int credits,
                   final int timetableNumber, final int colorRgb, final int registeredByUser) {
        this.mId                 = id;
        this.mDayOfWeek          = dayOfWeek;
        this.mPeriod             = period;
        this.mSubject            = subject;
        this.mInstructor         = instructor;
        this.mPlace              = place;
        this.mType               = type;
        this.mCredits            = credits;
        this.mTimetableNumber    = timetableNumber;
        this.mColorRgb           = colorRgb;
        this.hasRegisteredByUser = (registeredByUser == 1);
    }

    public void update(int dayOfWeek, int period, String subject, String instructor, String place, String type, int credits, int timetableNumber, int colorRgb) {
        this.mDayOfWeek       = dayOfWeek;
        this.mPeriod          = period;
        this.mSubject         = subject;
        this.mInstructor      = instructor;
        this.mPlace           = place;
        this.mType            = type;
        this.mCredits         = credits;
        this.mTimetableNumber = timetableNumber;
        this.mColorRgb        = colorRgb;

        PortalDataProvider.updateMyClass(mId, dayOfWeek, period, subject, instructor, place, type, credits, timetableNumber, colorRgb);
    }

    public boolean delete() {
        return PortalDataProvider.deleteMyClassById(mId);
    }

    public boolean equals(MyClass myClass){
        boolean matchString = StringUtils.equals(mSubject, myClass.getSubject()) && StringUtils.equals(mInstructor, myClass.getInstructor()) && StringUtils.equals(mPlace, myClass.getPlace()) && StringUtils.equals(mType , myClass.getType());

        return (mDayOfWeek == myClass.getDayOfWeek() && mPeriod == myClass.getPeriod() && mCredits == myClass.getCredits() && mTimetableNumber == myClass.getTimeTableNumber() && mColorRgb == myClass.getColor()) &&matchString;
    }

    /*
    Getter
     */
    public boolean equalTimetable(int dayOfWeek, int period){
        return (mDayOfWeek == dayOfWeek) && (mPeriod == period);
    }
    public int getId() {
        return mId;
    }

    public int getDayOfWeek() {
        return mDayOfWeek;
    }

    public int getPeriod() {
        return mPeriod;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getInstructor() {
        return mInstructor;
    }

    public String getPlace() {
        return mPlace;
    }

    public String getType() {
        return mType;
    }

    public int getCredits() {
        return mCredits;
    }

    public String getWebSyllabusUrl() {
        return WEB_SYLLABUS_TEMPLATE + mTimetableNumber;
    }

    public int getTimeTableNumber() {
        return mTimetableNumber;
    }

    public int getColor(){ return mColorRgb; }

    public boolean hasRegisteredByUser() {
        return hasRegisteredByUser;
    }
}
