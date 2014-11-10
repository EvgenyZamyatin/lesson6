package odeen.newrssreader.proj.conroller;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.List;

import odeen.newrssreader.proj.model.Channel;
import odeen.newrssreader.proj.model.Item;

/**
 * Created by Женя on 03.11.2014.
 */
public class ChannelManager {
    private final static String TAG = "ChannelManager";


    private static ChannelManager sManager;
    private Context mContext;
    private Uri mChannelsUri = Uri.parse("content://odeen.newrssreader.providers.channels_provider/channels");
    private Uri mItemsUri = Uri.parse("content://odeen.newrssreader.providers.channels_provider/items");

    private ChannelManager(Context context) {
        this.mContext = context;
    }

    public static ChannelManager get(Context context) {
        if (sManager == null) {
            sManager = new ChannelManager(context.getApplicationContext());
            return sManager;
        }
        return sManager;
    }

    //--------------
    // Channels
    //--------------

    public long insertChannel(String name, String link) {
        ContentValues cv = new ContentValues();
        cv.put(ChannelContentProvider.COLUMN_CHANNELS_CHANNEL_NAME, name);
        cv.put(ChannelContentProvider.COLUMN_CHANNELS_CHANNEL_LINK, link);
        long id = Long.parseLong(mContext.getContentResolver().insert(mChannelsUri, cv).getLastPathSegment());
        return id;
    }

    public Channel getChannelById(long channelId) {
        Channel channel = null;
        Uri uri = ContentUris.withAppendedId(mChannelsUri, channelId);
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        ChannelContentProvider.ChannelCursor channelCursor = new ChannelContentProvider.ChannelCursor(cursor, mContext);
        channelCursor.moveToFirst();
        if (!channelCursor.isAfterLast() && !channelCursor.isBeforeFirst())
            channel = channelCursor.getChannel();
        return channel;
    }

    public ChannelContentProvider.ChannelCursor queryChannels() {
        Cursor cursor = mContext.getContentResolver().query(mChannelsUri, null, null, null, null);
        return new ChannelContentProvider.ChannelCursor(cursor, mContext);
    }

    public void removeChannel(long id) {
        Uri uri = ContentUris.withAppendedId(mChannelsUri, id);
        mContext.getContentResolver().delete(uri, null, null);
    }

    public void updateChannel(long id, String name, String link) {
        Uri uri = ContentUris.withAppendedId(mChannelsUri, id);
        ContentValues cv = new ContentValues();
        cv.put(ChannelContentProvider.COLUMN_CHANNELS_CHANNEL_NAME, name);
        cv.put(ChannelContentProvider.COLUMN_CHANNELS_CHANNEL_LINK, link);
        mContext.getContentResolver().update(uri, cv, null, null);
    }

    public long insertOrUpdateChannel(long id, String name, String link) {
        if (id == -1)
            return insertChannel(name, link);
        else {
            updateChannel(id, name, link);
            return  id;
        }

    }

    public void setChannelLoading(long id, boolean isLoading) {
        Uri uri = ContentUris.withAppendedId(mChannelsUri, id);
        ContentValues cv = new ContentValues();
        cv.put(ChannelContentProvider.COLUMN_CHANNELS_IS_LOADING, isLoading ? 1 : 0);
        mContext.getContentResolver().update(uri, cv, null, null);
    }

    public boolean getChannelLoading(long id) {
        Uri uri = ContentUris.withAppendedId(mChannelsUri, id);
        Cursor c = mContext.getContentResolver().query(uri, new String[]{ChannelContentProvider.COLUMN_CHANNELS_IS_LOADING}
                , null, null, null, null);
        c.moveToFirst();
        if (!c.isAfterLast() || !c.isBeforeFirst())
            return false;
        boolean res = c.getInt(c.getColumnIndex(ChannelContentProvider.COLUMN_CHANNELS_IS_LOADING)) == 1;
        return res;
    }

    //--------------
    // Items
    //--------------

    public ChannelContentProvider.ItemCursor queryItems(long id) {
        Cursor cursor = mContext.getContentResolver().query(mItemsUri, null
                , ChannelContentProvider.COLUMN_ITEMS_CHANNEL_ID +  " = ?", new String[]{String.valueOf(id)}, null);
        Log.d(TAG, cursor.getCount()+"");
        return new ChannelContentProvider.ItemCursor(cursor, mContext);
    }

    public long getSessionId(long channelId) {
        Uri uri = mItemsUri;
        Cursor c = mContext.getContentResolver().query(
                uri
                , null
                , ChannelContentProvider.COLUMN_ITEMS_CHANNEL_ID + " = ?"
                , new String[]{String.valueOf(channelId)}
                , null
                , null);
        c.moveToFirst();
        if (c.isAfterLast() || c.isBeforeFirst() || c.getCount() == 0)
            return 0;
        return c.getLong(c.getColumnIndex(ChannelContentProvider.COLUMN_ITEMS_SESSION_ID));
    }

    public void insertFreshItems(List<Item> items, long channelId, long sessionId) {
        ContentValues[] cvs = new ContentValues[items.size()];
        for (int it = 0; it < items.size(); it++) {
            Item i = items.get(it);
            ContentValues cv = new ContentValues();
            cv.put(ChannelContentProvider.COLUMN_ITEMS_SESSION_ID, sessionId);
            cv.put(ChannelContentProvider.COLUMN_ITEMS_CHANNEL_ID, channelId);
            cv.put(ChannelContentProvider.COLUMN_ITEMS_TITLE, i.getTitle());
            cv.put(ChannelContentProvider.COLUMN_ITEMS_LINK, i.getLink());
            cv.put(ChannelContentProvider.COLUMN_ITEMS_DESCRIPTION, i.getDescription());
            cv.put(ChannelContentProvider.COLUMN_ITEMS_PUBDATE, i.getPubDate().getTime());
            cvs[it] = cv;
        }
        Log.d(TAG, "session id = " + sessionId);
        mContext.getContentResolver().bulkInsert(mItemsUri, cvs);
    }

    public void removeItems(long channelId, long sessionId) {
        int cnt = mContext.getContentResolver().delete(mItemsUri
                , ChannelContentProvider.COLUMN_ITEMS_CHANNEL_ID + " = ? and "
                        + ChannelContentProvider.COLUMN_ITEMS_SESSION_ID + " = ?"
                , new String[]{String.valueOf(channelId), String.valueOf(sessionId)});
        Log.d(TAG, "removed " + cnt);
    }

    public void updateItem(long channelId, Item i) {
        ContentValues cv = new ContentValues();
        cv.put(ChannelContentProvider.COLUMN_ITEMS_CHANNEL_ID, channelId);
        cv.put(ChannelContentProvider.COLUMN_ITEMS_TITLE, i.getTitle());
        cv.put(ChannelContentProvider.COLUMN_ITEMS_LINK, i.getLink());
        cv.put(ChannelContentProvider.COLUMN_ITEMS_DESCRIPTION, i.getDescription());
        cv.put(ChannelContentProvider.COLUMN_ITEMS_PUBDATE, i.getPubDate().getTime());
        cv.put(ChannelContentProvider.COLUMN_ITEMS_IS_WATCHED, i.isWatched() ? 1 : 0);
        int cnt = mContext.getContentResolver().update(mItemsUri, cv
                , ChannelContentProvider.COLUMN_ITEMS_CHANNEL_ID + " = ? and "
                        + ChannelContentProvider.COLUMN_ITEMS_LINK + " = ?"
                , new String[] {String.valueOf(channelId), i.getLink()});
        Log.d(TAG, "updated " + cnt);
    }

    public void removeItemsByChannelId(long id) {
        mContext.getContentResolver().delete(mItemsUri
                , ChannelContentProvider.COLUMN_ITEMS_CHANNEL_ID + " = ?"
                , new String[]{String.valueOf(id)});
    }



}
