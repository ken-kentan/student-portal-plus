package jp.kentan.student_portal_plus.data.component;


import jp.kentan.student_portal_plus.data.PortalDataProvider;
import jp.kentan.student_portal_plus.util.StringUtils;

public class News {

    public final static String URL = "https://portal.student.kit.ac.jp";
    public final static String KEY = "news";
    public final static String KEY_DATE = "news_last_date";

    private final int mId;

    private boolean hasRead, isFavorite;
    private final String mDate;
    private final String mInCharge;
    private final String mCategory;
    private final String mTitle;
    private final String mDetail;
    private final String mLink;
    private String mDetailText = "";


    public News(int id, String date, String inCharge, String category, String title, String detail, String link, int hasRead, int isFavorite) {
        this.mId        = id;
        this.hasRead    = (hasRead    > 0);
        this.isFavorite = (isFavorite > 0);
        this.mDate      = date;
        this.mInCharge  = inCharge;
        this.mCategory  = category;
        this.mTitle     = title;
        this.mDetail    = detail;
        this.mLink      = link;

        if (StringUtils.isEmpty(detail)) {
            mDetailText = link;
        } else {
            mDetailText = StringUtils.removeHtmlTag(detail);
        }
    }

    public boolean equals(final String date, final String inCharge, final String category, final String title, String detail, String link) {
        return this.mDate.equals(date) && mInCharge.equals(inCharge) && mCategory.equals(category) && this.mTitle.equals(title) && mDetail.equals(detail) && mLink.equals(link);
    }

    public boolean delete() {
        return PortalDataProvider.deleteNews(mId);
    }


    /*
    Setter
     */
    public void setRead(boolean read) {
        this.hasRead = read;
        PortalDataProvider.updateNewsStatus(mId, hasRead, isFavorite);
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
        PortalDataProvider.updateNewsStatus(mId, hasRead, isFavorite);
    }


    /*
    Getter
     */
    public boolean hasRead() {
        return hasRead;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public int getId() {
        return mId;
    }

    public String getDate() {
        return mDate.replaceAll("-", "/");
    }

    public String getInCharge(){ return mInCharge; }

    public String getCategory(){ return mCategory; }

    public String getTitle() {
        return mTitle;
    }

    public String getDetail() {
        return mDetail;
    }

    public String getDetailText() {
        return mDetailText;
    }

    public String getLink() {
        return mLink;
    }
}
