package jp.kentan.student_portal_plus.data.component;


import jp.kentan.student_portal_plus.data.PortalDataProvider;

public class LectureCancellation {

    public final static String URL = "https://portal.student.kit.ac.jp/ead/?c=lecture_cancellation";
    public final static String KEY = "lecture_cancel";
    public final static String KEY_DATE = "lecture_cancellation_last_date";

    public final static int ALL = 0, MY_CLASS = 1;

    public final static int RESISTED_BY_PORTAL  = 0;
    public final static int RESISTED_BY_USER    = 1;
    public final static int RESISTED_BY_SIMILAR = 2;

    private int mId, mMyClassStatus;
    private boolean hasRead;
    private final String mReleaseDate, mCancelDate, mFaculty, mSubject, mInstructor, mDayOfWeek, mPeriod, mNote;


    public LectureCancellation(final int id, final String releaseDate, final String cancelDate, final String faculty, final String subject, final String instructor,
                               final String dayOfWeek, final String period, final String note, final int read, final int myClassStatus) {
        this.mId            = id;
        this.mReleaseDate   = releaseDate;
        this.mCancelDate    = cancelDate;
        this.mFaculty       = faculty;
        this.mSubject       = subject;
        this.mInstructor    = instructor;
        this.mDayOfWeek     = dayOfWeek;
        this.mPeriod        = period;
        this.mNote          = note;
        this.hasRead        = (read == 1);
        this.mMyClassStatus = myClassStatus;
    }

    public boolean equals(String releaseDate, String cancelDate, String faculty, String subject, String instructor, String dayOfWeek, String period, String note){
        return mReleaseDate.equals(releaseDate) && mCancelDate.equals(cancelDate) && mFaculty.equals(faculty) && mSubject.equals(subject) && mInstructor.equals(instructor) && mDayOfWeek.equals(dayOfWeek) &&
                mPeriod.equals(period) && mNote.equals(note);
    }


    /*
    Setter
     */
    public void setRead(boolean read) {
        this.hasRead = read;
        PortalDataProvider.updateLectureCancelStatus(mId, read);
    }

    public boolean setMyClass(boolean isMyClass) {
        if ((mMyClassStatus >= RESISTED_BY_PORTAL && mMyClassStatus < RESISTED_BY_SIMILAR) == isMyClass || mMyClassStatus == RESISTED_BY_PORTAL) return true;

        if (isMyClass) {
            PortalDataProvider.registerToMyClass(mSubject, mInstructor, mDayOfWeek, mPeriod);
            this.mMyClassStatus = RESISTED_BY_USER;
        } else {
            if(!PortalDataProvider.unregisterFromMyClass(mSubject)){
                return false;
            }
            this.mMyClassStatus = -1;
        }

        return true;
    }


    /*
    Getter
     */
    public boolean hasRead() {
        return hasRead;
    }

    public boolean isMyClass() {
        return mMyClassStatus != -1;
    }

    public int getId(){ return mId; }

    public String getReleaseDate(){ return mReleaseDate.replaceAll("-", "/"); }

    public String getCancelDate(){ return mCancelDate.replaceAll("-", "/"); }

    public String getDate(){
        return "掲示年月日: " + getReleaseDate();
    }

    public String getSubject(){
        return mSubject;
    }

    public String getInstructor(){ return mInstructor; }

    public String getFaculty(){ return mFaculty; }

    public String getDayAndPeriod() {
        final String dayOfWeek = mDayOfWeek.replaceFirst("曜日", "曜 ");

        if (dayOfWeek.equals("集中") || dayOfWeek.equals("-")) {
            return dayOfWeek;
        } else {
            return dayOfWeek + mPeriod + "限";
        }

    }

    public String getNote(){ return mNote; }

    public int getMyClassStatus() {
        return mMyClassStatus;
    }
}
