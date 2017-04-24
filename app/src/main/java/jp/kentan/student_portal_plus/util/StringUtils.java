package jp.kentan.student_portal_plus.util;


import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.text.style.StyleSpan;

import java.util.regex.Pattern;

public class StringUtils {

    //support custom SPAN class (https://portal.student.kit.ac.jp/css/common/wb_common.css)
    private final static Pattern[] HTML_TAGS = new Pattern[]{
            Pattern.compile("<span class=\"col_red\">(.*?)</span>"),
            Pattern.compile("<span class=\"col_green\">(.*?)</span>"),
            Pattern.compile("<span class=\"col_blue\">(.*?)</span>"),
            Pattern.compile("<span class=\"col_orange\">(.*?)</span>"),
            Pattern.compile("<span class=\"col_white\">(.*?)</span>"),
            Pattern.compile("<span class=\"col_black\">(.*?)</span>"),
            Pattern.compile("<span class=\"col_gray\">(.*?)</span>"),
            Pattern.compile("<a href=\"(.*?)\"(.*?)\">(.*?)</a>"),
            Pattern.compile("<span class=\"u_line\">(.*?)</span>"),
            Pattern.compile("([A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}?)")
    };

    private final static String[] TAG_REPLACES = new String[]{
            "<font color=\"#ff0000\">$1</font>",
            "<font color=\"#008000\">$1</font>",
            "<font color=\"#0000ff\">$1</font>",
            "<font color=\"#ffa500\">$1</font>",
            "<font color=\"#ffffff\">$1</font>",
            "<font color=\"#000000\">$1</font>",
            "<font color=\"#999999\">$1</font>",
            "$3( $1 )",
            "<u>$1</u>",
            " $1 "
    };

    public final static StyleSpan BOLD = new StyleSpan(Typeface.BOLD);


    public static boolean equals(String string1, String string2) {
        if (string1 == null){
            return (string2 == null);
        }

        return string1.equals(string2);
    }

    public static boolean isEmpty(String string) {
        return string == null || string.length() <= 0;
    }

    public static boolean isBlank(String string) {
        return string == null || trim(string).length() <= 0;
    }

    public static String[] splitWithSpace(String string) {
        return string.replaceAll("　", " ").split(" ");
    }

    public static Spanned fromHtml(String html) {
        for (int i = 0; i < 10; ++i) {
            html = HTML_TAGS[i].matcher(html).replaceAll(TAG_REPLACES[i]);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    public static String removeHtmlTag(String html) {
        html = html.replaceAll("<br>" , "  ");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }

    public static int getStartPeriod(String string) throws Exception {
        String[] split;

        if (string.contains("～")) {
            split = string.split("～");
        } else {
            split = string.split("-");
        }

        return Integer.parseInt(split[0]);
    }

    public static int getEndPeriod(String string) throws Exception {
        String[] split;

        if (string.contains("～")) {
            split = string.split("～");
        } else {
            split = string.split("-");
        }

        return Integer.parseInt(split[1]);
    }

    public static String escapeQuery(String string){
        return string.replaceAll("'", "''").replaceAll("%", "\\$%").replaceAll("_", "\\$_");
    }

    private static String trim(String value) {
        if (value == null || value.length() == 0) return value;

        int st = 0;
        int len = value.length();
        char[] val = value.toCharArray();
        while ((st < len) && ((val[st] <= ' ') || (val[st] == '　'))) {
            st++;
        }
        while ((st < len) && ((val[len - 1] <= ' ') || (val[len - 1] == '　'))) {
            len--;
        }
        return ((st > 0) || (len < value.length())) ? value.substring(st, len) : value;
    }
}
