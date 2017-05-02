package jp.kentan.student_portal_plus.notification;


import android.text.Spannable;
import android.text.SpannableString;

import jp.kentan.student_portal_plus.util.StringUtils;

public class Content {

    public enum TYPE{LECTURE_INFO, LECTURE_CANCEL, NEWS}

    final static String NAME[] = {"授業関連連絡", "休講情報", "最新情報"};

    private final String mTitle, mText;

    private final TYPE mType;

    private String mHash; //DBから参照するための検索用ハッシュ


    public Content(TYPE type, String title, String text, String hash){
        this.mType = type;

        this.mTitle = title;
        this.mText  = text;

        this.mHash = hash;
    }

    /*
    Static methods
     */
    public static String parseTextWithLectureCancel(String cancelDate, String instructor, String note){
        return cancelDate.replaceAll("-", "/") + "  " + ((note.length() > 1) ? StringUtils.removeHtmlTag(note) : instructor);
    }

    public static String parseTextWithNew(String detail, String link){
        return StringUtils.isBlank(detail) ? link : StringUtils.removeHtmlTag(detail);
    }

    static String getInboxStyleTitle(TYPE type, int size) {
        return size + "件の" + NAME[type.ordinal()];
    }


    /*
    Getter
     */
    public TYPE getType(){ return mType; }

    public String getTitle(){ return mTitle; }
    public String getText(){ return mText; }

    String getHash(){ return mHash; }


    Spannable getInboxStyleText() {
        Spannable spannable = new SpannableString(mTitle + " " + mText);
        spannable.setSpan(StringUtils.BOLD, 0, mTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
