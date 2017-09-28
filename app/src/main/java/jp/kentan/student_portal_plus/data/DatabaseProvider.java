package jp.kentan.student_portal_plus.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.kentan.student_portal_plus.R;
import jp.kentan.student_portal_plus.data.component.LectureCancellation;
import jp.kentan.student_portal_plus.data.component.LectureInformation;
import jp.kentan.student_portal_plus.data.component.MyClass;
import jp.kentan.student_portal_plus.data.component.News;
import jp.kentan.student_portal_plus.util.JaroWinklerDistance;


public class DatabaseProvider {

    private final static String TAG = "DatabaseProvider";

    final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);

    private final static String DB_NAME = "portal_data.db";
    private final static int DB_VERSION = 2;

    //table name
    final static String NEWS_TABLE = "news";
    final static String LECTURE_INFO_TABLE = "lecture_info";
    final static String LECTURE_CANCEL_TABLE = "lecture_cancel";
    final static String MY_CLASS_TABLE     = "my_class";

    //columns
    private final static String NEWS_COLUMNS = "(_id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT NOT NULL, in_charge TEXT NOT NULL, category TEXT NOT NULL," +
            " title TEXT NOT NULL, detail TEXT NOT NULL, link TEXT NOT NULL, read INTEGER NOT NULL, favorite INTEGER NOT NULL)";
    private final static String LECTURE_INFO_COLUMNS = "(_id INTEGER PRIMARY KEY AUTOINCREMENT, release_date TEXT NOT NULL, update_date TEXT NOT NULL," +
            " faculty TEXT NOT NULL, semester TEXT NOT NULL, subject TEXT NOT NULL, instructor TEXT NOT NULL, day_of_week TEXT NOT NULL, period TEXT NOT NULL," +
            " type TEXT NOT NULL, detail TEXT NOT NULL, read INTEGER NOT NULL)";
    private final static String LECTURE_CANCEL_COLUMNS = "(_id INTEGER PRIMARY KEY AUTOINCREMENT, release_date TEXT NOT NULL, cancel_date TEXT NOT NULL," +
            " faculty TEXT NOT NULL, subject TEXT NOT NULL, instructor TEXT NOT NULL, day_of_week TEXT NOT NULL, period TEXT NOT NULL, note TEXT NOT NULL," +
            " read INTEGER NOT NULL)";
    private final static String MY_CLASS_COLUMNS = "(_id INTEGER PRIMARY KEY AUTOINCREMENT, day_of_week INTEGER NOT NULL, period INTEGER NOT NULL," +
            " subject TEXT NOT NULL, instructor TEXT, place TEXT, type TEXT, credits INTEGER NOT NULL, timetable_number INTEGER NOT NULL," +
            " color_rgb INTEGER NOT NULL, registered_by_user INTEGER NOT NULL)";

    private final static JaroWinklerDistance JARO_WINKLER_DISTANCE = new JaroWinklerDistance();

    private static DatabaseProvider sInstance = null;
    private SQLiteDatabase mDatabase = null;

    private static boolean hasShowDialog = false;
    private static AlertDialog.Builder DIALOG_DB_BROKEN;

    private float mMyClassThreshold = 0.8f;


    @SuppressLint("ShowToast")
    private DatabaseProvider(final Context context) {
        mDatabase = new DatabaseHelper(context).getWritableDatabase();
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + NEWS_TABLE         + " " + NEWS_COLUMNS);
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + LECTURE_INFO_TABLE + " " + LECTURE_INFO_COLUMNS);
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + LECTURE_CANCEL_TABLE + " " + LECTURE_CANCEL_COLUMNS);
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + MY_CLASS_TABLE     + " " + MY_CLASS_COLUMNS);

        DIALOG_DB_BROKEN = new AlertDialog.Builder(context)
            .setIcon(R.drawable.ic_warning)
            .setTitle("データベース破損")
            .setMessage(context.getString(R.string.msg_warn_database_reset))
            .setNegativeButton("キャンセル", null)
            .setPositiveButton("初期化", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    hasShowDialog = false;
                    sInstance.resetAll();
                    Toast.makeText(context, "データベースを初期化しました", Toast.LENGTH_LONG).show();
                }
        });
    }

    public static DatabaseProvider getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseProvider(context);
        }

        return sInstance;
    }

    static SQLiteDatabase getDatabase() {
        return sInstance.mDatabase;
    }

    static void setMyClassThreshold(float threshold){
        sInstance.mMyClassThreshold = threshold;
    }

    public static long deleteAll() {
        sInstance.mDatabase.beginTransaction();

        long num = 0;
        try {
            num += sInstance.mDatabase.delete(NEWS_TABLE        , null, null);
            num += sInstance.mDatabase.delete(LECTURE_INFO_TABLE, null, null);
            num += sInstance.mDatabase.delete(LECTURE_CANCEL_TABLE, null, null);
            num += sInstance.mDatabase.delete(MY_CLASS_TABLE    , null, null);

            //Reset autoincrement
            sInstance.mDatabase.execSQL("DELETE FROM sqlite_sequence WHERE name='" + NEWS_TABLE + "' OR name='" + LECTURE_INFO_TABLE + "' OR name='" + MY_CLASS_TABLE + "'");

            sInstance.mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }

        return num;
    }

    private void resetAll(){
        mDatabase.beginTransaction();
        try {
            mDatabase.execSQL("DROP TABLE IF EXISTS " + NEWS_TABLE);
            mDatabase.execSQL("DROP TABLE IF EXISTS " + LECTURE_INFO_TABLE);
            mDatabase.execSQL("DROP TABLE IF EXISTS " + LECTURE_CANCEL_TABLE);
            mDatabase.execSQL("DROP TABLE IF EXISTS " + MY_CLASS_TABLE);

            //Reset autoincrement
            mDatabase.execSQL("DELETE FROM sqlite_sequence WHERE name='" + NEWS_TABLE + "' OR name='" + LECTURE_INFO_TABLE + "' OR name='" + LECTURE_CANCEL_TABLE + "' OR name='" + MY_CLASS_TABLE + "'");

            mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + NEWS_TABLE         + " " + NEWS_COLUMNS);
            mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + LECTURE_INFO_TABLE + " " + LECTURE_INFO_COLUMNS);
            mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + LECTURE_CANCEL_TABLE + " " + LECTURE_CANCEL_COLUMNS);
            mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + MY_CLASS_TABLE     + " " + MY_CLASS_COLUMNS);

            mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            mDatabase.endTransaction();
        }
    }

    void insertMyClass(String subject, String instructor, String place, int dayOfWeek, int period, String type, int credits, int timetableNum, int colorRgb, boolean isRegisteredByUser) {
        sInstance.mDatabase.beginTransaction();

        try {
            SQLiteStatement statement = sInstance.mDatabase.compileStatement("INSERT INTO " + MY_CLASS_TABLE + " VALUES(?,?,?,?,?,?,?,?,?,?,?);");

            statement.bindNull(1);
            statement.bindLong(2, dayOfWeek);
            statement.bindLong(3, period);
            statement.bindString(4, subject);

            if (instructor == null) statement.bindNull(5);
            else                    statement.bindString(5, instructor);

            if (place == null) statement.bindNull(6);
            else               statement.bindString(6, place);

            if (type == null) statement.bindNull(7);
            else              statement.bindString(7, type);

            statement.bindLong(8, credits);
            statement.bindLong(9, timetableNum);
            statement.bindLong(10, colorRgb);
            statement.bindLong(11, isRegisteredByUser ? 1 : 0);
            statement.executeInsert();

            sInstance.mDatabase.setTransactionSuccessful();
            statement.close();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }
    }

    List<String[]> select(final String query, final int columns) {
        List<String[]> list = new ArrayList<>();

        sInstance.mDatabase.beginTransaction();
        try {
            Cursor cursor = sInstance.mDatabase.rawQuery(query, null);

            while (cursor.moveToNext()) {
                String[] result = new String[columns];

                for (int index = 0; index < columns; ++index) {
                    result[index] = cursor.getString(index);
                }

                list.add(result);
            }

            sInstance.mDatabase.setTransactionSuccessful();
            cursor.close();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }

        return list;
    }

    List<News> selectNews(final String where) {
        List<News> list = new ArrayList<>();
        String sql;

        if (where == null) {
            sql = "SELECT * FROM " + NEWS_TABLE + " ORDER BY DATE(date) DESC, _id DESC";
        } else {
            sql = "SELECT * FROM " + NEWS_TABLE + " " + where + ((!where.contains("LIMIT 1") ? " ORDER BY DATE(date) DESC, _id DESC" : ""));
        }

        sInstance.mDatabase.beginTransaction();
        try {
            Cursor cursor = sInstance.mDatabase.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                list.add(new News(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5),
                        cursor.getString(6), cursor.getInt(7), cursor.getInt(8)));
            }

            sInstance.mDatabase.setTransactionSuccessful();
            cursor.close();
        } catch (SQLException | IllegalStateException e) {
            Log.e(TAG, e.getMessage(), e);

            if(e instanceof IllegalStateException && !hasShowDialog){
                hasShowDialog = true;
                DIALOG_DB_BROKEN.show();
            }
        } finally {
            sInstance.mDatabase.endTransaction();
        }

        return list;
    }

    List<LectureInformation> selectLectureInformation(final String where) {
        List<LectureInformation> list = new ArrayList<>();
        String sql;

        if (where == null) {
            sql = "SELECT * FROM " + LECTURE_INFO_TABLE + " ORDER BY DATE(update_date) DESC, subject ASC";
        } else {
            sql = "SELECT * FROM " + LECTURE_INFO_TABLE + " " + where + ((where.contains("LIMIT 1") ? "" : " ORDER BY DATE(update_date) DESC, subject ASC"));
        }

        sInstance.mDatabase.beginTransaction();
        try {
            List<String[]> myClassSubjectList = select("SELECT subject, registered_by_user FROM " + MY_CLASS_TABLE, 2);

            Cursor cursor = sInstance.mDatabase.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                String subject = cursor.getString(5);
                int myClassStatus = -1;

                //受講科目かどうか判定
                for (String[] tmp : myClassSubjectList) {
                    if (tmp[0].equals(subject)) {
                        myClassStatus = Integer.parseInt(tmp[1]);
                        break;
                    } else if(mMyClassThreshold < 1.0f && JARO_WINKLER_DISTANCE.getDistance(tmp[0], subject) >= mMyClassThreshold) {
                        myClassStatus = LectureInformation.RESISTED_BY_SIMILAR;
//                        Log.d(TAG, tmp[0] + ", " + subject + " => " + jaroWinklerDistance.getDistance(tmp[0], subject));
                    }
                }

                list.add(new LectureInformation(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), subject, cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9),
                        cursor.getString(10), cursor.getInt(11), myClassStatus));
            }

            sInstance.mDatabase.setTransactionSuccessful();
            cursor.close();
        } catch (SQLException | IllegalStateException e) {
            Log.e(TAG, e.getMessage(), e);

            if(e instanceof IllegalStateException && !hasShowDialog){
                hasShowDialog = true;
                DIALOG_DB_BROKEN.show();
            }
        } finally {
            sInstance.mDatabase.endTransaction();
        }

        return list;
    }

    List<LectureCancellation> selectLectureCancellation(final String where) {
        List<LectureCancellation> list = new ArrayList<>();
        String sql;

        if (where == null) {
            sql = "SELECT * FROM " + LECTURE_CANCEL_TABLE + " ORDER BY DATE(release_date) DESC, subject ASC";
        } else {
            sql = "SELECT * FROM " + LECTURE_CANCEL_TABLE + " " + where + ((!where.contains("LIMIT 1") ? " ORDER BY DATE(release_date) DESC, subject ASC" : ""));
        }

        sInstance.mDatabase.beginTransaction();
        try {
            List<String[]> myClassSubjectList = select("SELECT subject, registered_by_user FROM " + MY_CLASS_TABLE, 2);

            Cursor cursor = sInstance.mDatabase.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                String subject = cursor.getString(4);
                int myClassStatus = -1;

                //受講科目かどうか判定
                for (String[] tmp : myClassSubjectList) {
                    if (tmp[0].equals(subject)) {
                        myClassStatus = Integer.parseInt(tmp[1]);
                        break;
                    } else if(mMyClassThreshold < 1.0f && JARO_WINKLER_DISTANCE.getDistance(tmp[0], subject) >= mMyClassThreshold) {
                        myClassStatus = LectureInformation.RESISTED_BY_SIMILAR;
                    }
                }

                list.add(new LectureCancellation(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        subject, cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getInt(9), myClassStatus));
            }

            sInstance.mDatabase.setTransactionSuccessful();
            cursor.close();
        } catch (SQLException | IllegalStateException e) {
            Log.e(TAG, e.getMessage(), e);

            if(e instanceof IllegalStateException && !hasShowDialog){
                hasShowDialog = true;
                DIALOG_DB_BROKEN.show();
            }
        } finally {
            sInstance.mDatabase.endTransaction();
        }

        return list;
    }

    List<MyClass> selectMyClass(final String where) {
        List<MyClass> list = new ArrayList<>();
        String sql;

        if (where == null) {
            sql = "SELECT * FROM " + MY_CLASS_TABLE + " ORDER BY day_of_week ASC, period ASC";
        } else {
            sql = "SELECT * FROM " + MY_CLASS_TABLE + " " + where + " ORDER BY day_of_week ASC, period ASC";
        }

        sInstance.mDatabase.beginTransaction();
        try {
            Cursor cursor = sInstance.mDatabase.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                list.add(new MyClass(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getInt(8), cursor.getInt(9), cursor.getInt(10)));
            }

            sInstance.mDatabase.setTransactionSuccessful();
            cursor.close();
        } catch (SQLException | IllegalStateException e) {
            Log.e(TAG, e.getMessage(), e);

            if(e instanceof IllegalStateException && !hasShowDialog){
                hasShowDialog = true;
                DIALOG_DB_BROKEN.show();
            }
        } finally {
            sInstance.mDatabase.endTransaction();
        }

        return list;
    }

    void updateMyClass(String[] targetIds, int dayOfWeek, int period, String subject, String instructor, String place, String type, int credits, int timetableNumber, int colorRgb) {
        sInstance.mDatabase.beginTransaction();
        try {
            final ContentValues values = new ContentValues();

            values.put("day_of_week"     , dayOfWeek);
            values.put("period"          , period);
            values.put("subject"         , subject);
            values.put("instructor"      , instructor);
            values.put("place"           , place);
            values.put("type"            , type);
            values.put("credits"         , credits);
            values.put("timetable_number", timetableNumber);
            values.put("color_rgb"       , colorRgb);

            sInstance.mDatabase.update(MY_CLASS_TABLE, values, "_id=?", targetIds);

            sInstance.mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }
    }

    void updateNews(String[] targetIds, boolean read, boolean favorite) {
        sInstance.mDatabase.beginTransaction();
        try {
            final ContentValues values = new ContentValues();

            values.put("read",     (read)     ? 1 : 0);
            values.put("favorite", (favorite) ? 1 : 0);

            sInstance.mDatabase.update(NEWS_TABLE, values, "_id=?", targetIds);

            sInstance.mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }
    }

    void updateLectureInformation(String[] ids, boolean read) {
        sInstance.mDatabase.beginTransaction();
        try {
            final ContentValues values = new ContentValues();

            values.put("read", (read) ? 1 : 0);

            sInstance.mDatabase.update(LECTURE_INFO_TABLE, values, "_id=?", ids);

            sInstance.mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }
    }

    void updateLectureCancellation(String[] ids, boolean read) {
        sInstance.mDatabase.beginTransaction();
        try {
            final ContentValues values = new ContentValues();

            values.put("read", (read) ? 1 : 0);

            sInstance.mDatabase.update(LECTURE_CANCEL_TABLE, values, "_id=?", ids);

            sInstance.mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }
    }

//    boolean deleteMyClass(final String where) {
//        sInstance.mDatabase.beginTransaction();
//        try {
//            sInstance.mDatabase.execSQL("DELETE FROM " + MY_CLASS_TABLE + " " + where);
//
//            sInstance.mDatabase.setTransactionSuccessful();
//        } catch (SQLException e) {
//            Log.e(TAG, e.getMessage(), e);
//        } finally {
//            sInstance.mDatabase.endTransaction();
//        }
//    }

    boolean deleteMyClassBySubject(final String subject) {
        int rows = 0;
        sInstance.mDatabase.beginTransaction();
        try {
            rows = sInstance.mDatabase.delete(MY_CLASS_TABLE, "registered_by_user=1 AND subject=?", new String[]{subject});

            sInstance.mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }

        return rows > 0;
    }

    boolean deleteMyClassById(final int id) {
        int rows = 0;
        sInstance.mDatabase.beginTransaction();
        try {
            rows = sInstance.mDatabase.delete(MY_CLASS_TABLE, "_id=?", new String[]{Integer.toString(id)});

            sInstance.mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }

        return rows > 0;
    }

    boolean deleteNewsById(final int id) {
        int rows = 0;
        sInstance.mDatabase.beginTransaction();
        try {
            rows = sInstance.mDatabase.delete(NEWS_TABLE, "_id=?", new String[]{Integer.toString(id)});

            sInstance.mDatabase.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            sInstance.mDatabase.endTransaction();
        }

        return rows > 0;
    }


    private class DatabaseHelper extends SQLiteOpenHelper {

        private final static String TAG = "DatabaseHelper";
        private Context mContext;


        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);

            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + NEWS_TABLE           + " " + NEWS_COLUMNS);
                db.execSQL("CREATE TABLE IF NOT EXISTS " + LECTURE_INFO_TABLE   + " " + LECTURE_INFO_COLUMNS);
                db.execSQL("CREATE TABLE IF NOT EXISTS " + LECTURE_CANCEL_TABLE + " " + LECTURE_CANCEL_COLUMNS);
                db.execSQL("CREATE TABLE IF NOT EXISTS " + MY_CLASS_TABLE       + " " + MY_CLASS_COLUMNS);
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "onUpgrade");

            if(newVersion == 2){
                db.beginTransaction();
                try {
                    db.execSQL("CREATE TABLE tmp " + NEWS_COLUMNS);
                    db.execSQL("UPDATE " + NEWS_TABLE + " SET link = '' WHERE link IS NULL");
                    db.execSQL("INSERT INTO tmp SELECT * FROM " + NEWS_TABLE);
                    db.execSQL("DROP TABLE " + NEWS_TABLE);
                    db.execSQL("ALTER TABLE tmp RENAME TO " + NEWS_TABLE);

                    Toast.makeText(mContext, "データベースをアップグレードしました", Toast.LENGTH_LONG).show();

                    db.setTransactionSuccessful();
                } catch (Exception e){
                    Log.e(TAG, "failed to upgrade.", e);
                } finally {
                    db.endTransaction();
                }
            }
        }
    }
}
