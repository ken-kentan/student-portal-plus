package jp.kentan.student_portal_plus.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.kentan.student_portal_plus.data.component.LectureCancellation;
import jp.kentan.student_portal_plus.data.component.LectureInformation;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.data.component.News;
import jp.kentan.student_portal_plus.notification.Content;
import jp.kentan.student_portal_plus.data.shibboleth.AsyncShibbolethClient;


public class PortalDataProvider implements AsyncShibbolethClient.AuthCallback {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.JAPAN);

    private static boolean isFetching = false;

    private Callback mCallback;
    private AsyncShibbolethClient mShibbolethClient;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private List<String> mQueryList = new ArrayList<>();
    private Map<String, Document> mResultMap = new HashMap<>();

    private static MyClassManager sMyClass = null;
    private static LectureInformationManager sLectureInformation = null;
    private static LectureCancellationManager sLectureCancellation = null;
    private static NewsManager sNews = null;


    @SuppressLint("CommitPrefEdits")
    public PortalDataProvider(Context context, @Nullable Callback callback) {
        createManagersIfNeed(context);

        mShibbolethClient = new AsyncShibbolethClient(context, true, this);

        mCallback = callback;

        mPreferences = context.getSharedPreferences("update_content", Context.MODE_PRIVATE);
        mEditor      = mPreferences.edit();

        setMyClassThreshold(context.getSharedPreferences("common", Context.MODE_PRIVATE).getFloat("my_class_threshold", 0.8f));
    }

    private static void createManagersIfNeed(Context context) {
        final SharedPreferences pref = context.getSharedPreferences("common", Context.MODE_PRIVATE);
        final DatabaseProvider database = DatabaseProvider.getInstance(context);
        int newsNumLimit = pref.getInt("limit_of_latest_info", 100);

//        if(newsNumLimit < 200){
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putInt("limit_of_latest_info", newsNumLimit=200);
//            editor.apply();
//        }

        if (sMyClass == null) {
            sMyClass = new MyClassManager(database);
        }
        if (sLectureInformation == null) {
            sLectureInformation = new LectureInformationManager(database);
        }
        if(sLectureCancellation == null){
            sLectureCancellation = new LectureCancellationManager(database);
        }
        if (sNews == null) {
            sNews = new NewsManager(database, newsNumLimit);
        }
    }

    public static void setMyClassThreshold(float threshold){
        DatabaseProvider.setMyClassThreshold(threshold);
        sLectureInformation.setMyClassThreshold(threshold);
        sLectureCancellation.setMyClassThreshold(threshold);
    }

    /*
    MyClass
     */
    public static List<MyClass> getMyClassList() {
        return sMyClass.get();
    }

    public static MyClass getMyClassById(int id) throws Exception {
        return sMyClass.getById(id);
    }

    public static List<MyClass> getTimetable() {
        return sMyClass.getTimetable();
    }

    public static MyClassManager.WEEK getTimetableWeek() {
        return sMyClass.getTimetableWeek();
    }

    static List<String> getMyClassSubjectList() {
        return sMyClass.getSubjectList();
    }

    public static void updateMyClass(int id, int dayOfWeek, int period, String subject, String instructor, String place, String type, int credits, int timetableNumber, int colorRgb) {
        sMyClass.update(id, dayOfWeek, period, subject, instructor, place, type, credits, timetableNumber, colorRgb);
    }

    public static void registerToMyClass(String subject, String instructor, String place, String type, int dayOfWeek, int period, int credits, int timetableNumber, int colorRgb) {
        sMyClass.register(subject, instructor, place, type, dayOfWeek, period, credits, timetableNumber, colorRgb);
    }

    public static void registerToMyClass(String subject, String instructor, String strDayOfWeek, String strPeriod) {
        sMyClass.register(subject, instructor, strDayOfWeek, strPeriod);
    }

    public static boolean unregisterFromMyClass(String subject) {
        return sMyClass.unregister(subject);
    }

    public static boolean deleteMyClassById(int id) {
        return sMyClass.deleteById(id);
    }


    /*
    LectureInfo
     */
    public static List<LectureInformation> getLectureInfoList(int type) {
        if (type == LectureInformation.ALL) {
            return sLectureInformation.get();
        } else {
            return sLectureInformation.getWithMyClass();
        }
    }

    public static List<LectureInformation> getLectureInfoList(String[] searchWords, int sortBy, boolean unread, boolean read, boolean myClass) {
        return sLectureInformation.getWithFilter(searchWords, sortBy, unread, read, myClass);
    }

    public static LectureInformation getLectureInfoById(int id) throws Exception {
        return sLectureInformation.getById(id);
    }

    public static LectureInformation getLectureInfoByHash(Context context, String hash) throws Exception {
        createManagersIfNeed(context);

        return sLectureInformation.getByHash(hash);
    }

    public static List<Content> getFetchedLectureInfoList(int notifyType) {
        return sLectureInformation.getUnregisteredList(notifyType);
    }

    public static void updateLectureInfoStatus(int id, boolean wasRead) {
        sLectureInformation.updateReadStatus(id, wasRead);
    }


    /*
    Lecture Cancel
     */
    public static List<LectureCancellation> getLectureCancelList(int type) {
        if (type == LectureCancellation.ALL) {
            return sLectureCancellation.get();
        } else {
            return sLectureCancellation.getWithMyClass();
        }
    }

    public static List<LectureCancellation> getLectureCancelList(String[] searchWords, int sortBy, boolean unread, boolean read, boolean myClass) {
        return sLectureCancellation.getWithFilter(searchWords, sortBy, unread, read, myClass);
    }

    public static LectureCancellation getLectureCancelById(int id) throws Exception {
        return sLectureCancellation.getById(id);
    }

    public static LectureCancellation getLectureCancelByHash(Context context, String hash) throws Exception {
        createManagersIfNeed(context);

        return sLectureCancellation.getByHash(hash);
    }

    public static List<Content> getFetchedLectureCancelList(int notifyType) {
        return sLectureCancellation.getUnregisteredList(notifyType);
    }

    public static void updateLectureCancelStatus(int id, boolean wasRead) {
        sLectureCancellation.updateReadStatus(id, wasRead);
    }

    /*
    News
     */
    public static List<News> getNewsList() {
        return sNews.get();
    }

    public static List<News> getNewsList(String[] titleWords, int periodDate, boolean unread, boolean read, boolean favorite) {
        return sNews.getWithFilter(titleWords, periodDate, unread, read, favorite);
    }

    public static News getNewsById(int id) throws Exception {
        return sNews.getById(id);
    }

    public static News getNewsByHash(Context context, String hash) throws Exception {
        createManagersIfNeed(context);

        return sNews.getByHash(hash);
    }

    public static List<Content> getFetchedNews() {
        return sNews.getFetchedInfo();
    }

    public static void updateNewsStatus(int id, boolean wasRead, boolean isFavorite) {
        sNews.updateReadFavStatus(id, wasRead, isFavorite);
    }

    public static boolean deleteNews(int id) {
        return sNews.deleteById(id);
    }


    /*
    Fetch
     */
    public void fetch() {
        if (mPreferences.getBoolean("all", true)) {
            mQueryList.add(MyClass.URL);
            mQueryList.add(LectureInformation.URL);
            mQueryList.add(LectureCancellation.URL);
            mQueryList.add(News.URL);
        } else {
            if (mPreferences.getBoolean(MyClass.KEY, true)) {
                mQueryList.add(MyClass.URL);
            }
            if (mPreferences.getBoolean(LectureInformation.KEY, true)) {
                mQueryList.add(LectureInformation.URL);
            }
            if (mPreferences.getBoolean(LectureCancellation.KEY, true)) {
                mQueryList.add(LectureCancellation.URL);
            }
            if (mPreferences.getBoolean(News.KEY, true)) {
                mQueryList.add(News.URL);
            }
        }

        if (mQueryList.size() <= 0) {
            mCallback.failed("取得データがありません", null);
            return;
        }

        isFetching = true;
        mShibbolethClient.fetchDocument(mQueryList.get(0));
        mQueryList.remove(0);
    }

    public static boolean isFetching() {
        return isFetching;
    }

    private void finishAllQuery(){
        isFetching = false;

        for(Map.Entry<String, Document> entry : mResultMap.entrySet()){
            switch (entry.getKey()) {
                case MyClass.URL:
                    sMyClass.scrape(mCallback, entry.getValue());
                    break;
                case LectureInformation.URL:
                    sLectureInformation.scrape(mCallback, entry.getValue());
                    break;
                case LectureCancellation.URL:
                    sLectureCancellation.scrape(mCallback, entry.getValue());
                    break;
                case News.URL:
                    sNews.scrape(mCallback, entry.getValue());
                    break;
                default:
                    break;
            }
        }
        mEditor.apply();

        mResultMap.clear();
        mCallback.success();
    }

    private void cancelAllQuery() {
        isFetching = false;
        mResultMap.clear();
        mQueryList.clear();
    }


    @Override
    public void updateStatus(String status) {}

    @Override
    public void failed(AsyncShibbolethClient.FAILED_STATUS status, String errorMessage, Throwable error) {
        cancelAllQuery();
        mCallback.failed(errorMessage, error);
    }

    @Override
    public void success(String url, Document document) {
        String now = DATE_FORMAT.format(Calendar.getInstance().getTime());

        mResultMap.put(url, document);

        switch (url) {
            case MyClass.URL:
                mEditor.putString(MyClass.KEY_DATE, now);
                break;
            case LectureInformation.URL:
                mEditor.putString(LectureInformation.KEY_DATE, now);
                break;
            case LectureCancellation.URL:
                mEditor.putString(LectureCancellation.KEY_DATE, now);
                break;
            case News.URL:
                mEditor.putString(News.KEY_DATE, now);
                break;
            default:
                break;
        }

        if (mQueryList.size() > 0) {
            mShibbolethClient.fetchDocument(mQueryList.get(0));
            mQueryList.remove(0);
        } else {
            finishAllQuery();
        }
    }


    public interface Callback {
        void failed(final String errorMessage, Throwable error);

        void success();
    }
}
