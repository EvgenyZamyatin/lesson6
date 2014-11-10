package odeen.newrssreader.proj.model;

import java.util.ArrayList;

/**
 * Created by Женя on 03.11.2014.
 */
public class Channel {
    private String mChannelName;
    private String mChannelLink;
    private long mId;

    public Channel(){}
    public Channel(String name, String link) {
        mChannelName = name;
        mChannelLink = link;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getChannelLink() {
        return mChannelLink;
    }

    public void setChannelLink(String mChannelLink) {
        this.mChannelLink = mChannelLink;
    }

    public String getChannelName() {
        return mChannelName;
    }

    public void setChannelName(String mChannelName) {
        this.mChannelName = mChannelName;
    }
}
