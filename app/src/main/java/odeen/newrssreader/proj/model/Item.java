package odeen.newrssreader.proj.model;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Женя on 03.11.2014.
 */
public class Item {

    private long  mSessionId;
    private String mTitle;
    private Date mPubDate;
    private String mLink;
    private String mDescription;
    private boolean mWatched;


    public long getSessionId() {
        return mSessionId;
    }


    public void setSessionId(long id) {
        mSessionId = id;
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public Date getPubDate() {
        return mPubDate;
    }

    public void setPubDate(Date mPubDate) {
        this.mPubDate = mPubDate;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String mLink) {
        this.mLink = mLink;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public boolean isWatched() {
        return mWatched;
    }

    public void setWatched(boolean mWatched) {
        this.mWatched = mWatched;
    }

    @Override
    public boolean equals(Object o) {
        Item other = (Item)o;
        return mLink.equals(other.getLink());
    }
}
