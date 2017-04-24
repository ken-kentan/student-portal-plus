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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import jp.kentan.student_portal_plus.data.component.News;
import jp.kentan.student_portal_plus.notification.Content;
import jp.kentan.student_portal_plus.util.StringUtils;


class NewsManager {

    private final static String TAG = "NewsManager";
    private final static SimpleDateFormat PORTAL_DATE_FORMAT = new SimpleDateFormat("yyyy.M.d", Locale.JAPAN);

    private List<News> mCache = new ArrayList<>();
    private List<Content> mUnregisteredNewsList = new ArrayList<>();

    private DatabaseProvider mDatabase;


    NewsManager(DatabaseProvider database, int limit) {
        mDatabase = database;

        createCache();

        mDatabase.setLimitToNews(limit);
    }

    private void createCache() {
        mCache = mDatabase.selectNews(null);

        Log.d(TAG, "cache created. (" + mCache.size() + ")");
    }

    void scrape(PortalDataProvider.Callback callback, final Document document) {
        final Pattern patternInCharge = Pattern.compile("〈(.*?)〉");
        final Pattern patternCategory = Pattern.compile("《(.*?)》");

        final List<String> fetchedList = new ArrayList<>(); //取得情報をハッシュ化して保持
        mUnregisteredNewsList.clear();

        final Element h1Contents;
        try {
            h1Contents = document.body().children().select("div.h1_contents").get(0);
        } catch (Exception e){
            callback.failed("failed to scraping of news", e);
            Log.d(TAG, "Web scraping failed. :" + e);
            return;
        }

        createCache();

        final SQLiteDatabase database = DatabaseProvider.getDatabase();
        database.beginTransaction();

        try {
            final SQLiteStatement statement = database.compileStatement("INSERT INTO " + DatabaseProvider.NEWS_TABLE + " VALUES(?,?,?,?,?,?,?,?,?);");

            Elements dls = h1Contents.select("dl");
            Collections.reverse(dls);
            for (Element row : dls) {
                Elements dds = row.select("dd");

                if(dds.size() < 4) continue;

                final Date date;
                final String strDate;
                try {
                    date = PORTAL_DATE_FORMAT.parse(dds.get(0).text());
                    strDate = DatabaseProvider.DATE_FORMAT.format(date);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    continue;
                }

                final String inCharge = patternInCharge.matcher(dds.get(1).text()).replaceFirst("$1");
                final String category = patternCategory.matcher(dds.get(2).text()).replaceFirst("$1");

                final String title, strLink;

                Element link = dds.get(3).select("a").first();

                if (link != null) {
                    title = link.text();
                    strLink = link.attr("href");
                } else {
                    title = dds.get(3).html().split("<br>", 0)[0].replaceAll("\n", "");
                    strLink = "";
                }

                String detail = dds.get(3).select("p.notice_info").html();
                if (StringUtils.isEmpty(detail)) detail = "";

                final String hash = strDate + inCharge + category + title + detail + strLink;
                fetchedList.add(hash);

                //未受信のみ保存
                if (!hasFetched(strDate, inCharge, category, title, detail, strLink)) {
                    statement.bindNull(1);
                    statement.bindString(2, strDate );
                    statement.bindString(3, inCharge);
                    statement.bindString(4, category);
                    statement.bindString(5, title   );
                    statement.bindString(6, detail  );
                    statement.bindString(7, strLink );

                    statement.bindLong(8, 0);
                    statement.bindLong(9, 0);

                    statement.executeInsert();
                    statement.clearBindings();

                    mUnregisteredNewsList.add(new Content(Content.TYPE.NEWS, title, Content.parseTextWithNew(detail, strLink), hash));
                }
            }

            //掲載されていない古い情報を消去
            final int fetchedListSize = fetchedList.size();
            if (fetchedListSize > 0) {
                StringBuilder args = new StringBuilder("?");

                for (int i=1; i<fetchedListSize; ++i) {
                    args.append(",?");
                }

                final SQLiteStatement st = database.compileStatement("DELETE FROM " + DatabaseProvider.NEWS_TABLE +
                        " WHERE favorite=0 AND (date || in_charge || category || title || detail || link)  NOT IN (" + args.toString() + ");");

                int index = 1;
                for(String hash : fetchedList){
                    st.bindString(index++, hash);
                }

                int num = st.executeUpdateDelete();
                st.close();

                Log.d(TAG, "deleted. (" + num + ")");
            } else {
                database.delete(DatabaseProvider.NEWS_TABLE, null, null);
            }

            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            database.endTransaction();
        }

        Log.d(TAG, "updated. (" + mUnregisteredNewsList.size() + ")");
    }

    boolean deleteById(int id) {
        return mDatabase.deleteNewsById(id);
    }

    void updateReadFavStatus(int id, boolean read, boolean favorite) {
        mDatabase.updateNews(new String[]{Integer.toString(id)}, read, favorite);
    }


    /*
    Getter
     */
    List<News> get() {
        return mCache = mDatabase.selectNews(null);
    }

    List<News> getWithFilter(final String[] words, final int periodDate, boolean unread, boolean read, boolean favorite) {
        StringBuilder where = new StringBuilder("WHERE (");

        List<String> args = new ArrayList<>();
        for(String word : words){
            args.add(DatabaseUtils.sqlEscapeString("%" + StringUtils.escapeQuery(word) + "%"));
        }

        for (String arg : args) {
            where.append("title LIKE ");
            where.append(arg);
            where.append(" AND ");
        }

        where.delete(where.length() - 5, where.length());
        where.append(")");

        final Calendar calendar = Calendar.getInstance();
        switch (periodDate) {
            case 1://Today
                String today = DatabaseProvider.DATE_FORMAT.format(Calendar.getInstance().getTime());
                where.append(" AND date>=DATE('").append(today).append("')");
                break;
            case 2://this week
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.clear(Calendar.MINUTE);
                calendar.clear(Calendar.SECOND);
                calendar.clear(Calendar.MILLISECOND);

                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

                String thisWeek = DatabaseProvider.DATE_FORMAT.format(calendar.getTime());

                where.append(" AND date>=DATE('").append(thisWeek).append("')");
                break;
            case 3://This month
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);

                String thisMonth = DatabaseProvider.DATE_FORMAT.format(calendar.getTime());

                where.append(" AND date>=DATE('").append(thisMonth).append("')");
                break;
            case 4://This month
                calendar.set(calendar.get(Calendar.YEAR), 0, 1);

                String thisYear = DatabaseProvider.DATE_FORMAT.format(calendar.getTime());

                where.append(" AND date>=DATE('").append(thisYear).append("')");
                break;
            default:
                break;
        }

        if (unread && read && favorite) {
            return mDatabase.selectNews(where.toString());
        }

        if (favorite) {
            if (unread) {
                where.append(" AND ( read=0 OR favorite=1 )");
            } else if (read) {
                where.append(" AND ( read=1 OR favorite=1 )");
            } else {
                where.append(" AND ( favorite=1 )");
            }
        } else {
            if (unread && read) {
                where.append(" AND ( favorite!=1 )");
            } else if (unread) {
                where.append(" AND ( read=0 AND favorite!=1 )");
            } else if (read) {
                where.append(" AND ( read=1 AND favorite!=1 )");
            } else {
                where.append(" AND ( read=0 AND read=1 AND favorite!=1 )");
            }
        }

        return mDatabase.selectNews(where.toString());
    }

    News getById(final int id) throws Exception {
        String where = "WHERE _id=" + id + " LIMIT 1";

        return mDatabase.selectNews(where).get(0);
    }

    News getByHash(String hash) throws Exception {
        String where = "WHERE (date || in_charge || category || title || detail || link)=" + DatabaseUtils.sqlEscapeString(hash) + " LIMIT 1";

        return mDatabase.selectNews(where).get(0);
    }

    List<Content> getFetchedInfo() {
        return mUnregisteredNewsList;
    }


    private boolean hasFetched(String date, String inCharge, String category, String title, String detail, String link) {
        for (News info : mCache) {
            if (info.equals(date, inCharge, category, title, detail, link)) {
                return true;
            }
        }
        return false;
    }
}
