package jp.kentan.student_portal_plus.data;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import jp.kentan.student_portal_plus.notification.Content;
import jp.kentan.student_portal_plus.data.component.LectureCancellation;
import jp.kentan.student_portal_plus.notification.NotificationService;
import jp.kentan.student_portal_plus.util.JaroWinklerDistance;
import jp.kentan.student_portal_plus.util.StringUtils;


class LectureCancellationManager {

    private final static String TAG = "LectureCancelManager";

    private final static SimpleDateFormat PORTAL_DATE_FORMAT = new SimpleDateFormat("yyyy/M/d", Locale.JAPAN);

    private final static Comparator<LectureCancellation> ORDER_BY_MY_CLASS = new Comparator<LectureCancellation>() {
        @Override
        public int compare(LectureCancellation a1, LectureCancellation a2) {
            int b1 = a1.isMyClass() ? 1 : 0;
            int b2 = a2.isMyClass() ? 1 : 0;
            return b2 - b1;
        }
    };

    private final static JaroWinklerDistance JARO_WINKLER_DISTANCE = new JaroWinklerDistance();

    private List<LectureCancellation> mCache = new ArrayList<>();
    private List<Content> mUnregisteredInfoList = new ArrayList<>();

    private DatabaseProvider mDatabase;

    private float mMyClassThreshold = 0.8f;


    LectureCancellationManager(DatabaseProvider database) {
        mDatabase = database;

        createCache();
    }

    void setMyClassThreshold(float threshold){
        mMyClassThreshold = threshold;
    }


    private void createCache() {
        mCache = mDatabase.selectLectureCancellation(null);

        Log.d(TAG, "cache created. (" + mCache.size() + ")");
    }

    void scrape(PortalDataProvider.Callback callback, Document document){
        final List<String> fetchedList = new ArrayList<>(); //取得情報をハッシュ化して保持
        mUnregisteredInfoList.clear();

        final Element table = document.body().children().select("table#cancel_info_data_tbl" ).first();

        if(table == null) return;

        createCache();

        final SQLiteDatabase database = DatabaseProvider.getDatabase();
        database.beginTransaction();

        try {
            final SQLiteStatement statement = database.compileStatement("INSERT INTO " + DatabaseProvider.LECTURE_CANCEL_TABLE + " VALUES(?,?,?,?,?,?,?,?,?,?);");

            for (Element row : table.select("tr")) {
                Elements tds = row.select("td");

                if(tds.size() < 9) continue;

                final String cancelDate, releaseDate;

                final String faculty    = tds.get(1).text(); //学部名など
                final String subject    = tds.get(2).text(); //授業科目名
                final String instructor = tds.get(3).text(); //担当教員名
                final String dayOfWeek  = tds.get(5).text(); //曜日
                final String period     = tds.get(6).text(); //時限
                final String note       = tds.get(7).html(); //備考

                //Date format
                try {
                    cancelDate  = DatabaseProvider.DATE_FORMAT.format(PORTAL_DATE_FORMAT.parse(tds.get(4).text())); //休講年月日
                    releaseDate = DatabaseProvider.DATE_FORMAT.format(PORTAL_DATE_FORMAT.parse(tds.get(8).text())); //掲示年月日
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    continue;
                }

                final String hash = releaseDate + cancelDate + faculty + subject + instructor + dayOfWeek + period + note;
                fetchedList.add(hash);

                if(hasFetched(releaseDate, cancelDate, faculty, subject, instructor, dayOfWeek, period, note)){
                    Log.d(TAG, "EXIST:" + subject);
                    continue;
                }

                statement.bindNull(1);
                statement.bindString( 2, releaseDate);
                statement.bindString( 3, cancelDate);
                statement.bindString( 4, faculty);
                statement.bindString( 5, subject);
                statement.bindString( 6, instructor);
                statement.bindString( 7, dayOfWeek);
                statement.bindString( 8, period);
                statement.bindString( 9, note);
                statement.bindLong(  10, 0); //read
                statement.executeInsert();
                statement.clearBindings();

                //NEWの場合は通知リストに追加
                mUnregisteredInfoList.add(new Content(Content.TYPE.LECTURE_CANCEL, subject, Content.parseTextWithLectureCancel(cancelDate, instructor, note), hash));
                Log.d(TAG, "NEW:" + subject);
            }
            statement.close();

            //掲載されていない古い情報を消去
            final int fetchedListSize = fetchedList.size();
            if (fetchedListSize > 0) {
                StringBuilder args = new StringBuilder("?");

                for (int i=1; i<fetchedListSize; ++i) {
                    args.append(",?");
                }

                final SQLiteStatement st = database.compileStatement("DELETE FROM " + DatabaseProvider.LECTURE_CANCEL_TABLE +
                        " WHERE (release_date || cancel_date || faculty || subject || instructor || day_of_week || period || note) NOT IN (" + args.toString() + ");");

                int index = 1;
                for(String hash : fetchedList){
                    st.bindString(index++, hash);
                }

                int num = st.executeUpdateDelete();
                st.close();

                Log.d(TAG, "deleted. (" + num + ")");
            } else {
                database.delete(DatabaseProvider.LECTURE_CANCEL_TABLE, null, null);
            }

            database.setTransactionSuccessful();
        } catch (Exception e){
            callback.failed(e.getMessage(), e);
            Log.e(TAG, e.getMessage(), e);
        } finally {
            database.endTransaction();
        }

        Log.d(TAG, "updated. (" + mUnregisteredInfoList.size() + ")");
    }

    private boolean hasFetched(String releaseDate, String cancelDate, String faculty, String subject, String instructor, String dayOfWeek, String period, String note) {
        for (LectureCancellation info : mCache) {
            if (info.equals(releaseDate, cancelDate, faculty, subject, instructor, dayOfWeek, period, note)) {
                return true;
            }
        }
        return false;
    }

    void updateReadStatus(int id, boolean read) {
        mDatabase.updateLectureCancellation(new String[]{Integer.toString(id)}, read);
    }

    /*
    Getter
     */
    List<LectureCancellation> get() {
        return mCache = mDatabase.selectLectureCancellation(null);
    }

    LectureCancellation getById(int id) throws Exception {
        String where = "WHERE _id=" + id + " LIMIT 1";

        return mDatabase.selectLectureCancellation(where).get(0);
    }

    LectureCancellation getByHash(String hash) throws Exception {
        String where = "WHERE (release_date || cancel_date || faculty || subject || instructor || day_of_week || period || note)="
                + DatabaseUtils.sqlEscapeString(hash) + " LIMIT 1";

        return mDatabase.selectLectureCancellation(where).get(0);
    }

    List<LectureCancellation> getWithMyClass() {
        List<LectureCancellation> list = new ArrayList<>(get());

        for (int index = 0; index < list.size(); ) {
            LectureCancellation info = list.get(index);

            if (!info.isMyClass()) {
                list.remove(info);
            } else {
                ++index;
            }
        }

        return list;
    }

    List<LectureCancellation> getWithFilter(final String[] words, int sortBy, boolean unread, boolean read, boolean myClass) {
        StringBuilder where = new StringBuilder("WHERE ((");

        List<String> args = new ArrayList<>();
        for(String word : words){
            args.add(DatabaseUtils.sqlEscapeString("%" + StringUtils.escapeQuery(word) + "%"));
        }

        for (String arg : args) {
            where.append("subject LIKE ");
            where.append(arg);
            where.append(" AND ");
        }
        where.delete(where.length() - 5, where.length());
        where.append(") OR (");

        for (String arg : args) {
            where.append("instructor LIKE ");
            where.append(arg);
            where.append(" AND ");
        }
        where.delete(where.length() - 5, where.length());
        where.append(")) ");

        if (!myClass) {
            if (!unread && !read) {
                where.append(" AND read=0 AND read=1");
            } else if (!unread) {
                where.append(" AND read!=0");
            } else if (!read) {
                where.append(" AND read!=1");
            }
        }

        List<LectureCancellation> informations = mDatabase.selectLectureCancellation(where.toString());

        if (myClass) {
            for (int index = 0; index < informations.size(); ) {
                final LectureCancellation info = informations.get(index);
                final boolean wasRead = info.hasRead(), isMyClass = info.isMyClass();

                if (!unread && (!wasRead && !isMyClass)) {
                    informations.remove(info);
                    continue;
                }
                if (!read && (wasRead && !isMyClass)) {
                    informations.remove(info);
                    continue;
                }

                ++index;
            }

            if (sortBy >= 1) {
                Collections.sort(informations, ORDER_BY_MY_CLASS);
            }
        } else {
            for (int index = 0; index < informations.size(); ) {
                final LectureCancellation info = informations.get(index);

                if (info.isMyClass()) {
                    informations.remove(info);
                    continue;
                }

                ++index;
            }
        }

        return informations;
    }

    List<Content> getUnregisteredList(final int type) {
        List<Content> filteredList = new ArrayList<>();

        //受講科目のみ
        if (type == NotificationService.NOTIFY_WITH_MY_CLASS) {
            final List<String> mySubjectList = PortalDataProvider.getMyClassSubjectList();

            String subject;
            for (Content content : mUnregisteredInfoList) {
                subject = content.getText();

                for (String mySubject : mySubjectList) {
                    if (mySubject.equals(subject) || (mMyClassThreshold < 1.0f && (JARO_WINKLER_DISTANCE.getDistance(mySubject, subject) >= mMyClassThreshold))) {
                        filteredList.add(content);
                        break;
                    }
                }
            }

            return filteredList;
        } else {
            return mUnregisteredInfoList;
        }
    }
}
