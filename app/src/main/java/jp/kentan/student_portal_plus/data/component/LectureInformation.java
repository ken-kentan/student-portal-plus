package jp.kentan.student_portal_plus.data.component;

import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.util.StringUtils;


public class LectureInformation {

    public final static String URL = "https://portal.student.kit.ac.jp/ead/?c=lecture_information";
    public final static String KEY = "lecture_info";
    public final static String KEY_DATE = "lecture_info_last_date";

    public final static int ALL = 0, MY_CLASS = 1;

    public final static int RESISTED_BY_PORTAL = 0;
    public final static int RESISTED_BY_USER   = 1;
    public final static int RESISTED_BY_SIMILAR = 2;

    public final static String[] DAY_OF_WEEK = {"月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日", "集中", "-"};

    private int mId;
    private boolean hasRead;
    private int mMyClassStatus;
    private final String mFaculty, mSemester, mSubject, mDayOfWeek, mPeriod, mType, mReleaseDate;
    private String mUpdateDate, mInstructor, mDetail;
    private String mDetailText;

    public LectureInformation(final int id, final String releaseDate, final String updateDate, final String faculty, final String semester, final String subject, final String instructor,
                              final String dayOfWeek, final String period, final String type, final String detail, final int read, final int myClassStatus) {
        this.mId            = id;
        this.hasRead        = (read == 1);
        this.mFaculty       = faculty;
        this.mSemester      = semester;
        this.mSubject       = subject;
        this.mInstructor    = instructor;
        this.mDayOfWeek     = dayOfWeek;
        this.mPeriod        = period;
        this.mType          = type;
        this.mDetail        = detail;
        this.mReleaseDate   = releaseDate;
        this.mUpdateDate    = updateDate;
        this.mMyClassStatus = myClassStatus;

        mDetailText = StringUtils.removeHtmlTag(detail);
    }

    //detail,instructor,updateDate以外が一致
    public boolean contains(final String faculty, final String semester, final String subject, final String dayOfWeek, final String period, final String type, final String releaseDate) {
        return mFaculty.equals(faculty) && mSemester.equals(semester) && mSubject.equals(subject) && mDayOfWeek.equals(dayOfWeek) && mPeriod.equals(period) &&
                mType.equals(type) && mReleaseDate.equals(releaseDate);
    }


    /*
    Setter
     */
    public void setRead(boolean read) {
        this.hasRead = read;
        PortalDataProvider.updateLectureInfoStatus(mId, read);
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

    public void overwrite(String updateDate, String instructor, String detail){
        this.mUpdateDate = updateDate;
        this.mInstructor = instructor;
        this.mDetail = detail;

        mDetailText = StringUtils.removeHtmlTag(detail);
    }


    /*
    Getter
     */
    public boolean hasUpdated(final String updateDate) {
        return !mUpdateDate.equals(updateDate);
    }

    public boolean hasRead() {
        return hasRead;
    }

    public boolean isMyClass() {
        return mMyClassStatus != -1;
    }

    public int getMyClassStatus() {
        return mMyClassStatus;
    }

    public int getId() {
        return mId;
    }

    public String getUpdateDate() {
        return mUpdateDate.replaceAll("-", "/");
    }

    public String getSubject() {
        return mSubject;
    }

    public String getInstructor() {
        return mInstructor;
    }

    public String getFaculty(){ return mFaculty; }

    public String getSemester(){
        if(mSemester.equals("-")){
            return "";
        }

        if(mSemester.length() <= 1){
            return mSemester + "学期";
        }

        return mSemester;
    }

    public String getDayAndPeriod() {
        final String dayOfWeek = mDayOfWeek.replaceFirst("曜日", "曜 ");
        final String period = (mPeriod.equals("-")) ? "" : mPeriod + "限";

        if (dayOfWeek.equals("集中") || dayOfWeek.equals("-")) {
            return dayOfWeek;
        } else {
            return dayOfWeek + period;
        }
    }

    public String getType() {
        return mType;
    }

    public String getDetail() {
        return mDetail;
    }

    public String getDetailText() {
        return mDetailText;
    }

    public String getDate() {
        StringBuilder builder = new StringBuilder("初回掲示日: ");

        builder.append(mReleaseDate.replaceAll("-", "/"));

        if (!mReleaseDate.equals(mUpdateDate)) {
            builder.append("  最終更新日: ");
            builder.append(mUpdateDate.replaceAll("-", "/"));
        }

        return builder.toString();
    }

}
