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

    private final int ID;
    private int mMyClassStatus;
    private boolean hasRead;
    private final String RELEASE_DATE, CANCEL_DATE, FACULTY, SUBJECT, INSTRUCTOR, DAY_OF_WEEK, PERIOD, NOTE;


    public LectureCancellation(final int id, final String releaseDate, final String cancelDate, final String faculty, final String subject, final String instructor,
                               final String dayOfWeek, final String period, final String note, final int read, final int myClassStatus) {
        ID             = id;
        RELEASE_DATE   = releaseDate;
        CANCEL_DATE    = cancelDate;
        FACULTY       = faculty;
        SUBJECT       = subject;
        INSTRUCTOR    = instructor;
        DAY_OF_WEEK     = dayOfWeek;
        PERIOD        = period;
        NOTE          = note;
        hasRead        = (read == 1);
        mMyClassStatus = myClassStatus;
    }

    public boolean equals(String releaseDate, String cancelDate, String faculty, String subject, String instructor, String dayOfWeek, String period, String note){
        return RELEASE_DATE.equals(releaseDate) && CANCEL_DATE.equals(cancelDate) && FACULTY.equals(faculty) && SUBJECT.equals(subject) && INSTRUCTOR.equals(instructor) && DAY_OF_WEEK.equals(dayOfWeek) &&
                PERIOD.equals(period) && NOTE.equals(note);
    }


    /*
    Setter
     */
    public void setRead(boolean read) {
        this.hasRead = read;
        PortalDataProvider.updateLectureCancelStatus(ID, read);
    }

    public boolean setMyClass(boolean isMyClass) {
        if ((mMyClassStatus >= RESISTED_BY_PORTAL && mMyClassStatus < RESISTED_BY_SIMILAR) == isMyClass || mMyClassStatus == RESISTED_BY_PORTAL) return true;

        if (isMyClass) {
            PortalDataProvider.registerToMyClass(SUBJECT, INSTRUCTOR, DAY_OF_WEEK, PERIOD);
            this.mMyClassStatus = RESISTED_BY_USER;
        } else {
            if(!PortalDataProvider.unregisterFromMyClass(SUBJECT)){
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

    public int getId(){ return ID; }

    public String getReleaseDate(){ return RELEASE_DATE.replaceAll("-", "/"); }

    public String getCancelDate(){ return CANCEL_DATE.replaceAll("-", "/"); }

    public String getDate(){
        return "掲示年月日: " + getReleaseDate();
    }

    public String getSubject(){
        return SUBJECT;
    }

    public String getInstructor(){ return INSTRUCTOR; }

    public String getFaculty(){ return FACULTY; }

    public String getDayAndPeriod() {
        final String dayOfWeek = DAY_OF_WEEK.replaceFirst("曜日", "曜 ");

        if (dayOfWeek.equals("集中") || dayOfWeek.equals("-")) {
            return dayOfWeek;
        } else {
            return dayOfWeek + PERIOD + "限";
        }

    }

    public String getNote(){ return NOTE; }

    public int getMyClassStatus() {
        return mMyClassStatus;
    }
}
