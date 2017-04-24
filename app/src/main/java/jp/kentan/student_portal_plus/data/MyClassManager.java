package jp.kentan.student_portal_plus.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import jp.kentan.student_portal_plus.data.component.LectureInformation;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.util.StringUtils;

class MyClassManager {

    private final static String TAG = "MyClassManager";

    private List<MyClass> mCache = new ArrayList<>();

    private DatabaseProvider mDatabase;

    public enum WEEK{MON, TUE, WED, THU, FRI, SAT, SUN}


    MyClassManager(DatabaseProvider database) {
        mDatabase = database;

        createCache();
    }

    private void createCache() {
        mCache = mDatabase.selectMyClass(null);

        Log.d(TAG, "cache created. (" + mCache.size() + ")");
    }

    void scrape(PortalDataProvider.Callback callback, Document document) {
        int countUpdate = 0;
        List<String> fetchedTimetableNumList = new ArrayList<>();

        final Element table, table2;
        try {
            table  = document.body().children().select("table#enroll_data_tbl" ).get(0);
            table2 = document.body().children().select("table#enroll_data_tbl2").get(0); //集中科目
        } catch (Exception e){
            //受講登録情報は、次学期になると表示されないため、ユーザーが登録したもの以外を消去
            if(document.body().children().select("div.information_area") != null){
                DatabaseProvider.getDatabase().delete("my_class", "registered_by_user=0", null);
            }else{
                callback.failed("failed to scraping of class", e);
                Log.d(TAG, "Web scraping failed. :" + e);
            }

            return;
        }

        createCache();

        final SQLiteDatabase database = DatabaseProvider.getDatabase();
        database.beginTransaction();
        try {
            final SQLiteStatement statement = database.compileStatement("INSERT INTO " + DatabaseProvider.MY_CLASS_TABLE + " VALUES(?,?,?,?,?,?,?,?,?,?,?);");

            final Pattern patternFullWidthSpace = Pattern.compile("　");
            final Pattern patternCredit = Pattern.compile("単位");

            int dayOfWeek = 0;
            for (Element row : table.select("tr")) {
                Elements tds = row.select("td");

                if(tds.size() < 7) continue;

                for (int indexPeriod = 0; indexPeriod < 7; ++indexPeriod) {
                    Elements element = tds.get(indexPeriod).select("p:contains(単位)");
                    if (element == null) continue;

                    String datas[] = element.html().split("<br> ", 5);
                    if (datas.length < 5) continue;

                    Element linkElement = element.select("a").first();

                    final int timetableNum = Integer.parseInt(linkElement.text());
                    //int dayOfWeek
                    final int period = indexPeriod + 1;
                    final String subject = datas[3];
                    final String instructor = patternFullWidthSpace.matcher(datas[4]).replaceAll(" ");
                    final String type = datas[2];
                    final int credits = Integer.parseInt(patternCredit.matcher(datas[1]).replaceAll(""));

                    if (!exist(timetableNum)) {
                        statement.bindNull(1);
                        statement.bindNull(6);
                        statement.bindString(4, subject);
                        statement.bindString(5, instructor);
                        statement.bindString(7, type);
                        statement.bindLong( 2, dayOfWeek);
                        statement.bindLong( 3, period);
                        statement.bindLong( 8, credits);
                        statement.bindLong( 9, timetableNum);
                        statement.bindLong(10, MyClass.DEFAULT_COLOR);
                        statement.bindLong(11, MyClass.RESISTED_BY_PORTAL);
                        statement.executeInsert();
                        statement.clearBindings();

                        ++countUpdate;

                        Log.d(TAG, "NEW: " + subject);
                    } else {
                        Log.d(TAG, "EXIST: " + subject);
                    }

                    fetchedTimetableNumList.add(Integer.toString(timetableNum));
                }

                ++dayOfWeek;
            }

            //集中科目
            for (Element row : table2.select("tr")) {
                Elements tds = row.select("td");
                final int size = tds.size();

                if(size < 10) continue;

                for (int index = 0; index < size; ++index) {
                    Elements element = tds.get(index).select(":contains(単位)");
                    if (element == null) continue;

                    String datas[] = element.html().split("<br> ", 5);
                    if (datas.length < 5) continue;

                    Element linkElement = element.select("a").first();

                    final int timetableNum = Integer.parseInt(linkElement.text());
                    final String subject = datas[3];
                    final String instructor = patternFullWidthSpace.matcher(datas[4]).replaceAll(" ");
                    final String type = datas[2];
                    final int credits = Integer.parseInt(patternCredit.matcher(datas[1]).replaceAll(""));

                    if (!exist(timetableNum)) {
                        statement.bindNull(1);
                        statement.bindNull(6);
                        statement.bindString(4, subject);
                        statement.bindString(5, instructor);
                        statement.bindString(7, type);
                        statement.bindLong( 2, 7);  //集中
                        statement.bindLong( 3, -1); //時限なし
                        statement.bindLong( 8, credits);
                        statement.bindLong( 9, timetableNum);
                        statement.bindLong(10, MyClass.DEFAULT_COLOR);
                        statement.bindLong(11, MyClass.RESISTED_BY_PORTAL);
                        statement.executeInsert();
                        statement.clearBindings();

                        ++countUpdate;

                        Log.d(TAG, "NEW: " + subject);
                    } else {
                        Log.d(TAG, "EXIST: " + subject);
                    }

                    fetchedTimetableNumList.add(Integer.toString(timetableNum));
                }
            }

            final int fetchedListSize = fetchedTimetableNumList.size();

            //掲載されていない古い情報を消去
            if (fetchedListSize > 0) {
                StringBuilder where = new StringBuilder();

                for (int i = 0; i < fetchedListSize; ++i) {
                    where.append("timetable_number!=? AND ");
                }
                where.delete(where.length() - 5, where.length());
                where.append(" AND registered_by_user=0");

                database.delete(DatabaseProvider.MY_CLASS_TABLE, where.toString(), fetchedTimetableNumList.toArray(new String[0]));
            } else {
                database.delete(DatabaseProvider.MY_CLASS_TABLE, "registered_by_user=0", null);
            }

            statement.close();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            database.endTransaction();
        }

        Log.d(TAG, "MyClass updated. (" + countUpdate + ")");
    }

    void update(int id, int dayOfWeek, int period, String subject, String instructor, String place, String type, int credits, int timetableNumber, int colorRgb) {
        mDatabase.updateMyClass(new String[]{Integer.toString(id)}, dayOfWeek, period, subject, instructor, place, type, credits, timetableNumber, colorRgb);
    }

    void register(String subject, String instructor, String place, String type, int dayOfWeek, int period, int credits, int timetableNumber, int colorRgb) {
        mDatabase.insertMyClass(subject, instructor, place, dayOfWeek, period, type, credits, timetableNumber, colorRgb, true);
    }

    void register(String subject, String instructor, String strDayOfWeek, String strPeriod) {
        int dayOfWeek, period;

        //曜日=>整数
        for (dayOfWeek = 0; dayOfWeek < LectureInformation.DAY_OF_WEEK.length; ++dayOfWeek) {
            if (strDayOfWeek.equals(LectureInformation.DAY_OF_WEEK[dayOfWeek])) {
                break;
            }
        }

        //時限を正規化
        try {
            //連続時限の場合
            if (strPeriod.length() >= 3) {
                final int start = StringUtils.getStartPeriod(strPeriod);
                final int end = StringUtils.getEndPeriod(strPeriod);

                for (period = start; period <= end; ++period) {
                    register(subject, instructor, null, null, dayOfWeek, period, -1, -1, MyClass.DEFAULT_COLOR);
                }

                return;
            } else {
                period = Integer.parseInt(strPeriod);
            }

        } catch (Exception e) {
            period = -1;
            Log.w(TAG, e);
        }

        register(subject, instructor, null, null, dayOfWeek, period, -1, -1, MyClass.DEFAULT_COLOR);
    }

    boolean unregister(String subject) {
        return mDatabase.deleteMyClassBySubject(subject);
    }


    /*
    Getter
     */
    List<MyClass> get() {
        return mCache = mDatabase.selectMyClass(null);
    }

    List<MyClass> getTimetable() {
        String where = "WHERE day_of_week=" + getTimetableWeek().ordinal();
        return mDatabase.selectMyClass(where);
    }

    WEEK getTimetableWeek(){
        final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int dayOfWeek  = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2;

        if(hour >= 20) ++dayOfWeek; //午後8時以降は明日の時間割

        if(dayOfWeek < 0 || dayOfWeek > WEEK.FRI.ordinal()) return WEEK.MON; //土、日は月のに

        return WEEK.values()[dayOfWeek];
    }

    MyClass getById(final int id) throws Exception {
        String where = "WHERE _id=" + id;

        return mDatabase.selectMyClass(where).get(0);
    }

    List<String> getSubjectList() {
        List<String[]> list = mDatabase.select("SELECT subject FROM " + DatabaseProvider.MY_CLASS_TABLE, 1);

        List<String> subjectList = new ArrayList<>();

        for (String[] subject : list) {
            subjectList.add(subject[0]);
        }

        return subjectList;
    }


    boolean deleteById(int id) {
        return mDatabase.deleteMyClassById(id);
    }


    private boolean exist(final int timetableNum) {
        for (MyClass info : mCache) {
            if (timetableNum == info.getTimeTableNumber()) {
                return true;
            }
        }

        return false;
    }
}
