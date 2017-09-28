package jp.kentan.student_portal_plus.data;

import android.content.ContentValues;
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
import jp.kentan.student_portal_plus.data.component.LectureInformation;
import jp.kentan.student_portal_plus.notification.NotificationService;
import jp.kentan.student_portal_plus.util.JaroWinklerDistance;
import jp.kentan.student_portal_plus.util.StringUtils;


class LectureInformationManager {

    private final static String TAG = "LectureInfoManager";

    private final static SimpleDateFormat PORTAL_DATE_FORMAT = new SimpleDateFormat("yyyy/M/d", Locale.JAPAN);

    private final static Comparator<LectureInformation> ORDER_BY_MY_CLASS = new Comparator<LectureInformation>() {
        @Override
        public int compare(LectureInformation a1, LectureInformation a2) {
            int b1 = a1.isMyClass() ? 1 : 0;
            int b2 = a2.isMyClass() ? 1 : 0;
            return b2 - b1;
        }
    };

    private final static JaroWinklerDistance JARO_WINKLER_DISTANCE = new JaroWinklerDistance();

    private enum INFO_STATUS {EXIST, EXIST_UPDATED, NEW}

    private List<LectureInformation> mCache = new ArrayList<>();
    private final List<Content> UNREGISTERED_INFO_LIST = new ArrayList<>();

    private final DatabaseProvider DATABASE;

    private float mMyClassThreshold = 0.8f;


    LectureInformationManager(DatabaseProvider database) {
        DATABASE = database;

        createCache();
    }

    void setMyClassThreshold(float threshold){
        mMyClassThreshold = threshold;
    }


    private void createCache() {
        mCache = DATABASE.selectLectureInformation(null);

        Log.d(TAG, "cache created. (" + mCache.size() + ")");
    }

    void scrape(PortalDataProvider.Callback callback, Document document) {
        List<String> fetchedList = new ArrayList<>(); //取得情報をハッシュ化して保持
        UNREGISTERED_INFO_LIST.clear();

        final Element table = document.body().children().select("table#class_msg_data_tbl").first();

        if(table == null) return;

        createCache();

        final SQLiteDatabase database = DatabaseProvider.getDatabase();
        database.beginTransaction();

        try {
            final SQLiteStatement statement = database.compileStatement("INSERT INTO " + DatabaseProvider.LECTURE_INFO_TABLE + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?);");

            for (Element row : table.select("tr")) {
                Elements tds = row.select("td");

                if(tds.size() < 11) continue;

                final String releaseDate, updateDate;

                final String faculty    = tds.get(1).text(); //学部名など
                final String semester   = tds.get(2).text(); //学期
                final String subject    = tds.get(3).text(); //授業科目名
                final String instructor = tds.get(4).text(); //担当教員名
                final String dayOfWeek  = tds.get(5).text(); //曜日
                String period     = tds.get(6).text(); //時限
                final String type       = tds.get(7).text(); //分類
                final String detail     = tds.get(8).html(); //連絡事項

                //Date format
                try {
                    releaseDate = DatabaseProvider.DATE_FORMAT.format(PORTAL_DATE_FORMAT.parse(tds.get( 9).text())); //初回掲示日
                    updateDate  = DatabaseProvider.DATE_FORMAT.format(PORTAL_DATE_FORMAT.parse(tds.get(10).text())); //最終更新日
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    continue;
                }

                final String hash = releaseDate + updateDate + faculty + semester + subject + instructor + dayOfWeek + period + type + detail;
                fetchedList.add(hash);

                final int id;
                final InformationStatus infoStatus = getInfoStatus(faculty, semester, subject, instructor, dayOfWeek, period, type, detail, releaseDate, updateDate);
                switch (infoStatus.STATUS) {
                    case NEW:
                        statement.bindNull(1);
                        statement.bindString( 2, releaseDate);
                        statement.bindString( 3, updateDate);
                        statement.bindString( 4, faculty);
                        statement.bindString( 5, semester);
                        statement.bindString( 6, subject);
                        statement.bindString( 7, instructor);
                        statement.bindString( 8, dayOfWeek);
                        statement.bindString( 9, period);
                        statement.bindString(10, type);
                        statement.bindString(11, detail);
                        statement.bindLong(12, 0); //read
                        statement.executeInsert();
                        statement.clearBindings();

                        Log.d(TAG, "NEW: " + subject);
                        break;
                    case EXIST_UPDATED:
                        final ContentValues values = new ContentValues();
                        values.put("update_date", updateDate);
                        values.put("instructor" , instructor);
                        values.put("detail"     , detail);
                        values.put("read"       , 0);

                        id = infoStatus.INFO.getId();
                        database.update(DatabaseProvider.LECTURE_INFO_TABLE, values, "_id=?", new String[]{Integer.toString(id)});

                        Log.d(TAG, "EXIST_UPDATED: " + subject);
                        break;
                    case EXIST:
                        Log.d(TAG, "EXIST: " + subject);
                        continue;
                    default:
                        continue;
                }

                //NEWまたはEXIST_UPDATEDの場合は通知リストに追加
                UNREGISTERED_INFO_LIST.add(new Content(Content.TYPE.LECTURE_INFO, subject, StringUtils.removeHtmlTag(detail), hash));
            }
            statement.close();

            //掲載されていない古い情報を消去
            final int fetchedListSize = fetchedList.size();
            if (fetchedListSize > 0) {
                StringBuilder args = new StringBuilder("?");

                for (int i=1; i<fetchedListSize; ++i) {
                    args.append(",?");
                }

                final SQLiteStatement st = database.compileStatement("DELETE FROM " + DatabaseProvider.LECTURE_INFO_TABLE +
                        " WHERE (release_date || update_date || faculty || semester || subject || instructor || day_of_week || period || type || detail) NOT IN (" + args.toString() + ");");

                int index = 1;
                for(String hash : fetchedList){
                    st.bindString(index++, hash);
                }

                int num = st.executeUpdateDelete();
                st.close();

                Log.d(TAG, "deleted. (" + num + ")");
            } else {
                database.delete(DatabaseProvider.LECTURE_INFO_TABLE, null, null);
            }

            database.setTransactionSuccessful();
        } catch (Exception e) {
            callback.failed(e.getMessage(), e);
            Log.e(TAG, e.getMessage(), e);
        } finally {
            database.endTransaction();
        }

        Log.d(TAG, "updated. (" + UNREGISTERED_INFO_LIST.size() + ")");
    }

    void updateReadStatus(int id, boolean read) {
        DATABASE.updateLectureInformation(new String[]{Integer.toString(id)}, read);
    }


    /*
    Getter
     */
    List<LectureInformation> get() {
        return mCache = DATABASE.selectLectureInformation(null);
    }

    List<LectureInformation> getWithMyClass() {
        List<LectureInformation> informations = new ArrayList<>(get());

        for (int index = 0; index < informations.size(); ) {
            LectureInformation info = informations.get(index);

            if (!info.isMyClass()) {
                informations.remove(info);
            } else {
                ++index;
            }
        }

        return informations;
    }

    List<LectureInformation> getWithFilter(final String[] words, int sortBy, boolean unread, boolean read, boolean myClass) {
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

        List<LectureInformation> informations = DATABASE.selectLectureInformation(where.toString());

        if (myClass) {
            for (int index = 0; index < informations.size(); ) {
                final LectureInformation info = informations.get(index);
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
                final LectureInformation info = informations.get(index);

                if (info.isMyClass()) {
                    informations.remove(info);
                    continue;
                }

                ++index;
            }
        }

        return informations;
    }

    LectureInformation getById(final int id) throws Exception {
        String where = "WHERE _id=" + id + " LIMIT 1";

        return DATABASE.selectLectureInformation(where).get(0);
    }

    LectureInformation getByHash(String hash){
        String where = "WHERE (release_date || update_date || faculty || semester || subject || instructor || day_of_week || period || type || detail)=" + DatabaseUtils.sqlEscapeString(hash) + " LIMIT 1";

        Log.d(TAG, where);

        return DATABASE.selectLectureInformation(where).get(0);
    }

    List<Content> getUnregisteredList(final int type) {
        List<Content> filteredList = new ArrayList<>();

        //受講科目のみ
        if (type == NotificationService.NOTIFY_WITH_MY_CLASS) {
            final List<String> mySubjectList = PortalDataProvider.getMyClassSubjectList();

            String subject;
            for (Content content : UNREGISTERED_INFO_LIST) {
                subject = content.getTitle();

                for (String mySubject : mySubjectList) {
                    if (mySubject.equals(subject) || (mMyClassThreshold < 1.0f && (JARO_WINKLER_DISTANCE.getDistance(mySubject, subject) >= mMyClassThreshold))) {
                        filteredList.add(content);
                        break;
                    }
                }
            }

            return filteredList;
        } else {
            return UNREGISTERED_INFO_LIST;
        }
    }

    private InformationStatus getInfoStatus(String faculty, String semester, String subject, String instructor, String dayOfWeek, String period, String type, String detail, String releaseDate, String updateDate) {

        for (LectureInformation info : mCache) {
            if (info.contains(faculty, semester, subject, dayOfWeek, period, type, releaseDate)) {
                if (info.hasUpdated(updateDate)) {
                    info.overwrite(updateDate, instructor, detail);
                    return new InformationStatus(INFO_STATUS.EXIST_UPDATED, info);
                } else if(detail.equals(info.getDetail())) {
                    return new InformationStatus(INFO_STATUS.EXIST, info);
                }
            }
        }

        return new InformationStatus(INFO_STATUS.NEW, null);
    }


    private static class InformationStatus {
        final INFO_STATUS STATUS;
        final LectureInformation INFO;

        private InformationStatus(INFO_STATUS status, LectureInformation info) {
            STATUS = status;
            INFO = info;
        }
    }
}
